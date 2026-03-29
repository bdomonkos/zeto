# 🔧 1. build stage
FROM node:22-alpine AS builder

WORKDIR /app

# dependency-k
COPY package*.json ./
RUN npm install

# source
COPY . .

# build
RUN npm run build


# 🌐 2. production stage
FROM nginx:alpine

# build output bemásolása
COPY --from=builder /app/dist /usr/share/nginx/html

# opcionális: saját nginx config
EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]