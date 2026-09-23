# syntax=docker/dockerfile:1

FROM node:26.9-alpine AS build
WORKDIR /build
COPY package.json package-lock.json ./
RUN npm install
COPY . .
ARG API_BASE_URL=http://localhost:8080
RUN sed -i "s#http://localhost:8080#${API_BASE_URL}#g" src/environments/environment.ts
RUN npm run build

FROM nginx:1.27-alpine AS runtime
RUN addgroup -S facturador && adduser -S facturador -G facturador
COPY --from=build /build/dist/frontend/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
RUN chown -R facturador:facturador /usr/share/nginx/html /var/cache/nginx /var/run \
    && touch /var/run/nginx.pid \
    && chown facturador:facturador /var/run/nginx.pid
USER facturador
EXPOSE 80

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
    CMD wget -qO- http://localhost:80 || exit 1

CMD ["nginx", "-g", "daemon off;"]
