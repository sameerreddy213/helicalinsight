// Seeds a deterministic demo dataset for Helical Insight MongoDB connectivity checks.
// Runs once on first container init (empty data volume).
db = db.getSiblingDB('hi_demo');

db.customers.drop();
db.customers.insertMany([
  {
    _id: ObjectId('64b7f0c2a1b2c3d4e5f60701'),
    name: 'Ada Lovelace',
    email: 'ada@example.com',
    createdAt: ISODate('2024-01-15T10:00:00Z'),
    address: { city: 'London', zip: 'SW1A' },
    tags: ['math', 'computing'],
    active: true,
    score: 100
  },
  {
    _id: ObjectId('64b7f0c2a1b2c3d4e5f60702'),
    name: 'Grace Hopper',
    email: null,
    createdAt: ISODate('2024-02-01T12:30:00Z'),
    // address intentionally missing
    tags: ['navy', 'cobol'],
    active: false,
    score: 99
  },
  {
    _id: ObjectId('64b7f0c2a1b2c3d4e5f60703'),
    name: 'Alan Turing',
    email: 'alan@example.com',
    createdAt: ISODate('2024-03-10T08:15:00Z'),
    address: { city: 'Manchester' },
    tags: [],
    active: true
    // score intentionally missing
  }
]);

try {
  db.createUser({
    user: 'hi_app',
    pwd: 'hi_app_dev',
    roles: [{ role: 'readWrite', db: 'hi_demo' }]
  });
} catch (e) {
  // User may already exist on re-seed.
  print('hi_app user ensure: ' + e);
}
