"use strict";

const w = 960;
const h = 600;

const svg = d3.select("#viz")
  .append("svg")
  .attr("width", w)
  .attr("height", h)

const canvas = svg.append("foreignObject")
  .append('xhtml:canvas')
  .attr('id', 'canvas_viz')
  .attr("width", w)
  .attr("height", h)

const projection = d3.geoAlbersUsa();

const basemapSVG = svg.append("g")
  .classed("map", true);

d3.json("us.json", function(err, data) {
  const path = d3.geoPath()
    .projection(projection)

  basemapSVG
    .append("path")
    .datum(topojson.feature(data, data.objects.states))
    .attr("class", "state-boundary")
    .attr("d", path)
})


const radarQueue = d3.queue()
radarQueue.defer(d3.csv, "result.csv")
radarQueue.await(drawRadar)


// Draw Radar
function drawRadar(error, radarData) {
  const context = canvas.node().getContext("2d")
  const detachedContainer = document.createElement("custom");
  const dataContainer = d3.select(detachedContainer);


  const radarColorScale = d3.scaleLinear()
    .domain([0, 10, 20, 30, 35, 40, 45, 50, 55])
    .range(["#FFFFFF", "gray", "light blue", "#00FB90", "#00BB00", "#FFFF70", "#D0D060", "#FF6060", "#DA0000"])

  const dataBinding = dataContainer.selectAll("custom.rect")
    .data(radarData, (d) => d)

  dataBinding.enter()
    .append("custom")
    .classed("rect", true)

  drawCanvas(dataContainer, context, radarColorScale)
}

function drawCanvas(dataContainer, context, radarColorScale) {
  const radarElements = dataContainer.selectAll('custom.rect')

  radarElements.each((d) => {
    const val = parseFloat(d.value)
    const lat = parseFloat(d.lat)
    const lon = parseFloat(d.lon)

    if (val === 0) { return }
    const coords = projection([lon, lat])

    console.log(coords);

    context.beginPath()
    context.rect(coords[0], coords[1], 1, 1)
    context.fillStyle = radarColorScale(val)
    context.fill()
    context.closePath()
  })
}
