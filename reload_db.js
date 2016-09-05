const env = process.argv[2] || 'development';
const config = require('./knexfile');
const knex = require ('knex')(config[env]);
const exec = require('child_process').exec;
require('dotenv').load();

const wipeAws = () => {
  let bucket = getBucket(env);
  if (!bucket) {
    exit('Invalid environment "' + env + '"');
  }
  console.log('emptying bucket "' + bucket + '"...');
  return execute('aws s3api list-objects --bucket ' + bucket).then(({ Contents }) => {
    return deleteEachObject(bucket, Contents || []);
  });
};

const deleteEachObject = (bucket, contents) => {
  return () => {
    return Promise.all(contents.map(object => {
      console.log('deleting "' + object.Key + '"...');
      let command = 'aws s3api delete-object --bucket ' + bucket + ' --key ' + object.Key;
      return execute(command).then(() => console.log('"' + object.Key + '" has been deleted.'));
    }));
  };
};

const rollBackDB = () => {
  console.log('rolling back db to beginning state...');
  return knex.migrate.rollback().then(response => {
    if (response === 'none' || !response[0]) return Promise.resolve();
    return rollBack();
  });
};

const migrateLatest = () => {
  console.log('running knex migrations...');
  return knex.migrate.latest();
};

const execute = command => new Promise((resolve, reject) => {
  return exec(command, (err, data) => {
    if (err) return reject(err);
    try {
      resolve(JSON.parse(data));
    } catch (_) {
      resolve(data);
    }
  });
});

const getBucket = env => {
  return {
    development: 'photoapi.brenna.dev',
    integration: 'photoapi.brenna.int'
  }[env];
};

const exit = message => {
  if (message) {
    console.error(message);
    process.exit(1);
  }
  process.exit();
};



wipeAws().then(rollBackDB).then(migrateLatest).then(() => {
  console.log('Environement "' + env + '" has been successfully reset.');
  exit();
}).catch(err => {
  exit('An error occurred', err);
});
