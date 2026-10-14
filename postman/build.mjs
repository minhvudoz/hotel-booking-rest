import { readdirSync, writeFileSync } from 'node:fs';
import { variables, collectionPrerequest } from './src/lib.mjs';

const dir = new URL('./src/', import.meta.url);
const files = readdirSync(dir).filter(f => /^\d\d-.+\.mjs$/.test(f)).sort();
const item = await Promise.all(files.map(async f => (await import(new URL(f, dir))).default));

const collection = {
  info: {
    name: 'Hotel Booking REST API',
    description: 'Kiểm thử dịch vụ web RESTful quản lý đặt phòng khách sạn. Chạy toàn bộ collection theo thứ tự từ trên xuống.',
    schema: 'https://schema.getpostman.com/json/collection/v2.1.0/collection.json',
  },
  event: [{ listen: 'prerequest', script: { type: 'text/javascript', exec: collectionPrerequest } }],
  variable: Object.entries(variables).map(([key, value]) => ({ key, value })),
  item,
};

writeFileSync(new URL('./HotelBooking.postman_collection.json', import.meta.url), JSON.stringify(collection, null, 2) + '\n');
console.log(`Đã sinh collection: ${item.length} thư mục, ${item.reduce((n, f) => n + f.item.length, 0)} request`);
