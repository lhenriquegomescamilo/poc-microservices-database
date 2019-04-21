FROM node:8
WORKDIR /usr/src/app

COPY package*.json ./
COPY . .
RUN npm install -g yarn
RUN npm install -g ts-node
RUN yarn install
RUN npm run build

EXPOSE 3001

CMD ["npm", "run", "start-prod"]
