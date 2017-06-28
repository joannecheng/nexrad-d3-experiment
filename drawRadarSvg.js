function drawRadar(error, data) {
  const context = canvas.node().getContext('2d')
  var radarColorScale = d3.scaleLinear()
    .domain([0, 50])
    .range(["#FFFFB2", "#B10026"])

  svg.append('g')
    .selectAll('circle.marker')
    .data(radarData).enter()
    .append('circle')
    .classed('marker', true)
    .attr('r', 0.5)
    .attr('cx', 0)
    .attr('cy', 0)
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
