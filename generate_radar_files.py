#!/usr/bin/env
import csv
import math

import pyart.io
import numpy as np
import numpy.ma as ma

import geopy
from geopy.distance import VincentyDistance

def offset_by_meters(x,y,lat,lon):
    if x==y==0:
        return lat,lon
    dist = math.sqrt(x*x+y*y)
    bearing = math.atan2(y,x)

    origin = geopy.Point(lat, lon)
    destination = VincentyDistance(meters=dist).destination(origin, math.degrees(bearing))

    lat2, lon2 = destination.latitude, destination.longitude    
    return lat2,lon2

def save_as_csv(filename, data, level, append, extent=300, points=100):
    grids = pyart.map.grid_from_radars(
        (data,),
        grid_shape=(11, points, points),
        grid_limits=((0, 11000), (-extent*1000.0, extent*1000.0), (-extent*1000.0, extent*1000.0)),
        fields=["reflectivityqc"],
        refl_field="reflectivityqc",
        max_refl=100.0)
    center = [grids.axes["lat"]["data"][0], grids.axes["lon"]["data"][0]]
    date = grids.axes["time"]["units"].replace("seconds since ", "")

    ref = grids.fields["reflectivityqc"]["data"][level]

    x_dists = grids.axes["x_disp"]["data"]
    y_dists = grids.axes["y_disp"]["data"]

    data = np.array(grids.fields["reflectivityqc"]["data"][level])

    if append:
        csvfile = open(filename, "ab")
    else:
        csvfile = open(filename, "wb")
    writer = csv.writer(csvfile, delimiter=",", quotechar="|", quoting=csv.QUOTE_MINIMAL)

    if not append:
        writer.writerow(["lat", "lon", "value","date"])
    for (ix, iy), value in np.ndenumerate(data):
        if value != -9999.0:
            x = x_dists[ix]
            y = y_dists[iy]
            lat, lon = offset_by_meters(x, y, center[0], center[1])
            writer.writerow([lat,lon,value,date])

    csvfile.close()
    return data

def generate_csv(filename):
    radar = pyart.io.read_nexrad_archive("data/" + filename)

    refl_grid = radar.get_field(0, "reflectivity")
    rhohv_grid = radar.get_field(0, "cross_correlation_ratio")
    zdr_grid = radar.get_field(0, "differential_reflectivity")

    ref_low = np.less(refl_grid, 20)
    zdr_high = np.greater(np.abs(zdr_grid), 2.3)
    rhohv_low = np.less(rhohv_grid, 0.95)
    notweather = np.logical_or(ref_low, np.logical_or(zdr_high, rhohv_low))

    qcrefl_grid = ma.masked_where(notweather, refl_grid)

    qced = radar.extract_sweeps([0])
    qced.add_field_like("reflectivity", "reflectivityqc", qcrefl_grid)

    save_as_csv(filename + ".csv", qced, 5, False)

##### MAIN #####
import os
filelist = os.listdir("./data")

for filename in filelist:
    if "2017_08_25_KHGX" in filename:
        print "generating " + filename
        generate_csv(filename)
        print "done generating " + filename + ".csv"
