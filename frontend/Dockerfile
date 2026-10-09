# ==========================================
# Stage 1: Build React/Vite Bundle
# ==========================================
FROM public.ecr.aws/docker/library/node:20-alpine AS builder

WORKDIR /app

# Tận dụng cache của Docker cho node_modules
COPY package*.json ./
RUN npm ci --prefer-offline || npm install

# Sao chép toàn bộ source code and build
COPY . .
ARG VITE_API_BASE_URL=/api/v1
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
RUN npm run build

# ==========================================
# Stage 2: Serve static files with Nginx Alpine
# ==========================================
FROM public.ecr.aws/docker/library/nginx:1.27-alpine

LABEL maintainer="routine-dev"
LABEL description="Routine Fashion Frontend Web Service"

# Cài đặt múi giờ Việt Nam
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Ho_Chi_Minh /etc/localtime && \
    echo "Asia/Ho_Chi_Minh" > /etc/timezone

# Use Nginx only as a static file server; API traffic is not proxied here.
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Sao chép các file static HTML/JS/CSS đã build từ Stage 1
COPY --from=builder /app/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
