"use strict";

const w = 960;
const h = 600;

let counter = 0;
const svg = d3.select("#viz")
  .append("svg")
  .attr("width", w)
  .attr("height", h)
  .on("click", () => {
    if (counter !== 0) {
      d3.select("#canvas_viz_" + (counter-1))
        .style("display", "none");
    }
    const radarToShow = d3.select("#canvas_viz_" + counter);
    radarToShow.style("display", "inline");
    counter += 1;
  })
const projection = d3.geoAlbersUsa();

const basemapSVG = svg.append("g")
  .classed("map", true)

d3.json("us.json", function(err, data) {
  const path = d3.geoPath()
    .projection(projection)

  basemapSVG
    .append("path")
    .datum(topojson.feature(data, data.objects.states))
    .attr("class", "state-boundary")
    .attr("d", path)
})

// Load and display radar

const radarQueue = d3.queue()

radarQueue.defer(d3.csv, "radar/2017_08_25_KHGX_KHGX20170825_000459_V06.csv")
radarQueue.defer(d3.csv, "radar/2017_08_25_KHGX_KHGX20170825_001046_V06.csv")
radarQueue.defer(d3.csv, "radar/2017_08_25_KHGX_KHGX20170825_001633_V06.csv")
radarQueue.defer(d3.csv, "radar/2017_08_25_KHGX_KHGX20170825_002221_V06.csv")
radarQueue.defer(d3.csv, "radar/2017_08_25_KHGX_KHGX20170825_002808_V06.csv")
radarQueue.defer(d3.csv, "radar/2017_08_25_KHGX_KHGX20170825_003355_V06.csv")
radarQueue.defer(d3.csv, "radar/2017_08_25_KHGX_KHGX20170825_003943_V06.csv")
radarQueue.defer(d3.csv, "radar/2017_08_25_KHGX_KHGX20170825_004530_V06.csv")
radarQueue.defer(d3.csv, "radar/2017_08_25_KHGX_KHGX20170825_005118_V06.csv")
radarQueue.defer(d3.csv, "radar/2017_08_25_KHGX_KHGX20170825_005639_V06.csv")
radarQueue.defer(d3.csv, "radar/2017_08_25_KHGX_KHGX20170825_010226_V06.csv")
radarQueue.defer(d3.csv, "radar/2017_08_25_KHGX_KHGX20170825_010748_V06.csv")
radarQueue.defer(d3.csv, "radar/2017_08_25_KHGX_KHGX20170825_011242_V06.csv")
radarQueue.defer(d3.csv, "radar/2017_08_25_KHGX_KHGX20170825_011736_V06.csv")
radarQueue.defer(d3.csv, "radar/2017_08_25_KHGX_KHGX20170825_012230_V06.csv")
radarQueue.defer(d3.csv, "radar/2017_08_25_KHGX_KHGX20170825_012725_V06.csv")
radarQueue.defer(d3.csv, "radar/2017_08_25_KHGX_KHGX20170825_013219_V06.csv")
radarQueue.defer(d3.csv, "radar/2017_08_25_KHGX_KHGX20170825_013713_V06.csv")
radarQueue.defer(d3.csv, "radar/2017_08_25_KHGX_KHGX20170825_014234_V06.csv")
radarQueue.defer(d3.csv, "radar/2017_08_25_KHGX_KHGX20170825_014728_V06.csv")
radarQueue.defer(d3.csv, "radar/2017_08_25_KHGX_KHGX20170825_015249_V06.csv")
radarQueue.defer(d3.csv, "radar/2017_08_25_KHGX_KHGX20170825_015744_V06.csv")
radarQueue.defer(d3.csv, "radar/2017_08_25_KHGX_KHGX20170825_020239_V06.csv")
radarQueue.defer(d3.csv, "radar/2017_08_25_KHGX_KHGX20170825_020734_V06.csv")
radarQueue.defer(d3.csv, "radar/2017_08_25_KHGX_KHGX20170825_021229_V06.csv")
radarQueue.defer(d3.csv, "radar/2017_08_25_KHGX_KHGX20170825_021750_V06.csv")
radarQueue.defer(d3.csv, "radar/2017_08_25_KHGX_KHGX20170825_022244_V06.csv")
radarQueue.defer(d3.csv, "radar/2017_08_25_KHGX_KHGX20170825_022739_V06.csv")
radarQueue.defer(d3.csv, "radar/2017_08_25_KHGX_KHGX20170825_023233_V06.csv")
radarQueue.defer(d3.csv, "radar/2017_08_25_KHGX_KHGX20170825_023727_V06.csv")
radarQueue.defer(d3.csv, "radar/2017_08_25_KHGX_KHGX20170825_024248_V06.csv")
radarQueue.defer(d3.csv, "radar/2017_08_25_KHGX_KHGX20170825_024742_V06.csv")
radarQueue.defer(d3.csv, "radar/2017_08_25_KHGX_KHGX20170825_025303_V06.csv")
radarQueue.defer(d3.csv, "radar/2017_08_25_KHGX_KHGX20170825_025824_V06.csv")
radarQueue.defer(d3.csv, "radar/2017_08_25_KHGX_KHGX20170825_030344_V06.csv")
radarQueue.defer(d3.csv, "radar/2017_08_25_KHGX_KHGX20170825_030558_V06.csv")
radarQueue.defer(d3.csv, "radar/2017_08_25_KHGX_KHGX20170825_031128_V06.csv")
radarQueue.defer(d3.csv, "radar/2017_08_25_KHGX_KHGX20170825_031710_V06.csv")
radarQueue.defer(d3.csv, "radar/2017_08_25_KHGX_KHGX20170825_032448_V06.csv")

radarQueue.await(loadRadar)

function loadRadar(error) {
  if (error) {
    console.error("there was an error", error)
    return
  }

  const radarScans = Array.prototype.slice.call(arguments, 1);
  for (let index in radarScans) {
    drawRadar(radarScans[index], index);
  }
}

// Draw Radar
function drawRadar(radarData, index) {
  const radarColorScale = d3
    .scaleLinear()
    .domain([0, 10, 20, 30, 35, 40, 45, 50, 55])
    .range([ "#FFFFFF", "#808080", "#ADD8E6", "#00FB90", "#00BB00", "#FFFF70", "#D0D060", "#FF6060", "#DA0000", ]);

  const canvas = svg
    .append("foreignObject")
    .attr("x", 0)
    .attr("y", 0)
    .attr("width", "100%")
    .attr("height", "100%")
    .append("xhtml:canvas")
    .attr("id", "canvas_viz_" + index)
    .attr("width", w)
    .attr("height", h);
  //.style('display', 'none')

  const context = canvas.node().getContext("2d");
  const detachedContainer = document.createElement("custom");
  const dataContainer = d3.select(detachedContainer);

  const dataBinding = dataContainer
    .selectAll("custom.rect")
    .data(radarData, (d) => d);

  dataBinding.enter().append("custom").classed("rect", true);

  drawCanvas(dataContainer, context, radarColorScale);
}

function drawCanvas(dataContainer, context, radarColorScale) {
  const radarElements = dataContainer.selectAll('custom.rect')

  radarElements.each((d) => {
    const val = parseFloat(d.value)
    const lat = parseFloat(d.lat)
    const lon = parseFloat(d.lon)

    if (val === 0) { return }
    const coords = projection([lon, lat])

    context.beginPath()
    context.rect(coords[0], coords[1], 1, 1)
    context.fillStyle = radarColorScale(val)
    context.fill()
    context.closePath()
  })
}
