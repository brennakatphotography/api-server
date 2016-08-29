try {
  require('dotenv').load();
} catch (err) {

}

module.exports = {
  development: {
    client: 'mysql',
    connection: {
      host: 'localhost',
      database: 'photo_api',
      user:     'root',
      password: 'root'
    }
  },
  integration: {
    client: 'mysql',
    connection: process.env.DATABASE_URL_INT
  },
  production: {
    client: 'mysql',
    connection: process.env.DATABASE_URL_PROD
  }
};
