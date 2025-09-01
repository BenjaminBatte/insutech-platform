# Step 1: Build Vite app
FROM node:20 AS builder
WORKDIR /app

# Accept build-time ARG for API base URL (default to localhost:8181)
ARG VITE_API_URL=http://localhost:8181
ENV VITE_API_URL=$VITE_API_URL

# Install deps
COPY package*.json ./
RUN npm install

# Copy source
COPY . .

# Write env var into .env.production (Vite will use this during build)
RUN echo "VITE_API_URL=$VITE_API_URL" > .env.production

# Build app
RUN npm run build

# Step 2: Serve with Nginx
FROM nginx:alpine

# Copy nginx.conf and strip BOM just in case
COPY nginx.conf /tmp/nginx.conf
RUN sed '1s/^\xEF\xBB\xBF//' /tmp/nginx.conf > /etc/nginx/conf.d/default.conf

# Copy built app
COPY --from=builder /app/dist /usr/share/nginx/html

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
