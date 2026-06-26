FROM node:22-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci --silent
COPY . .
ARG BUILD_CONFIG=production
RUN npm run build -- --configuration ${BUILD_CONFIG}

FROM nginx:alpine
COPY --from=build /app/dist/aratech-movies-frontend/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
