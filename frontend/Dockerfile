# --- Build Stage ---
FROM node:20 AS build
WORKDIR /app

# Package-Dateien kopieren und Dependencies installieren
COPY package*.json ./
RUN npm ci

# Restlichen Code kopieren
COPY . .

# Angular-App "learnhub" für Production builden
RUN npx ng build learnhub --configuration production

# --- Run Stage ---
FROM nginx:alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Nur die gebaute App in Nginx reinkopieren
COPY --from=build /app/dist/learnhub/browser /usr/share/nginx/html

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
