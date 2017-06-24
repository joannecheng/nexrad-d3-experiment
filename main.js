"use strict";

var w = 960;
var h = 600;

var svg = d3.select("#viz")
  .append("svg")
  .attr("width", w)
  .attr("height", h)
var projection = d3.geoAlbersUsa();

var basemapSVG = svg.append("g")
  .classed("map", true);

d3.json("us.json", function(err, data) {
  var path = d3.geoPath()
    .projection(projection)

  basemapSVG
    .append("path")
    .datum(topojson.feature(data, data.objects.states))
    .attr("class", "state-boundary")
    .attr("d", path)
})


var radarQueue = d3.queue();
radarQueue.defer(d3.csv, "result.csv")
radarQueue.await(drawRadar)


// Draw Radar
function drawRadar() {
  var data = Array.prototype.slice.call(arguments, 1)

  for (var i in data) {
    var radarData = data[i];
    var radarColorScale = d3.scaleLinear()
      .domain([0, 50])
      .range(["#FFFFB2", "#B10026"])

    var radarMarkers = svg.append("g")
      .selectAll("circle.marker")
      .data(radarData).enter()
      .append("circle")
      .classed("marker", true)
      .attr("r", 0.5)
      .attr("cx", 0)
      .attr("cy", 0)
      .attr("fill", function(d) {
        var val = parseFloat(d["value"]);
        if (val === 0) {
          return "none";
        }
        return radarColorScale(val);
      })
      .attr("transform", function(d) {
        var lat = parseFloat(d["lat"]);
        var lon = parseFloat(d["lon"]);
        var coordinates = projection([lon, lat]);

        return "translate(" + coordinates + ")";
      });
  }
}
