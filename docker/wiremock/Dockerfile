# Imagen de WireMock con los stubs de RecepcionComprobantesOffline y
# AutorizacionComprobantesOffline horneados adentro, para poder desplegarla
# como servicio independiente en Render (que no ofrece volumenes persistentes
# en el plan free).
FROM wiremock/wiremock:3.9.2-alpine
COPY mappings /home/wiremock/mappings
CMD ["--global-response-templating"]
