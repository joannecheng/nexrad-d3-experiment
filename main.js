"use strict";

var w = 960;
var h = 600;

var svg = d3.select("#viz")
  .append("svg")
  .attr("width", w)
  .attr("height", h)

const canvas = svg.append("foreignObject")
  .append('xhtml:canvas')
  .attr('id', 'canvas_viz')
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
function drawRadar(error, radarData) {
  const context = canvas.node().getContext('2d')
  var radarColorScale = d3.scaleLinear()
    $(document).bind('keydown', 'v', showObs);
    .domain([0, 10, 20, 30, 35, 40, 45, 50, 55])
    .range(["#FFFFFF", "gray", "light blue", "#00FB90", "#00BB00", "#FFFF70", "#D0D060", "#FF6060", "#DA0000"]);

  radarData.forEach((d) => {
    const val = parseFloat(d.value)
    if (val === 0) { return }

    const lat = parseFloat(d.lat)
    const lon = parseFloat(d.lon)
    const coords = projection([lon, lat])

    context.beginPath()
    context.rect(coords[0], coords[1], 1, 1)
    context.fillStyle = radarColorScale(val)
    context.fill()
    context.closePath()
  })
}
