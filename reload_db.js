var env = process.argv[2] || 'development';
var config = require('./knexfile');
var knex = require ('knex')(config[env]);
var exec = require('child_process').exec;
require('dotenv').load();



wipeAws().then(rollBack).then(latest).then(() => {
  console.log('Environement "' + env + '" has been successfully reset.');
  process.exit();
}).catch(err => {
  console.error('An error occurred', err);
  process.exit(1);
});



function wipeAws() {
  let bucket = getBucket(env);
  return execute('aws s3api list-objects --bucket ' + bucket).then(result => {
    let contents = result.Contents || [];
    return Promise.all(contents.map(object => {
      return execute('aws s3api delete-object --bucket ' + bucket + ' --key ' + object.Key);
    }));
  }).then(result => {
    console.log('AWS Bucket "' + bucket + '" has been wiped.');
  });
}

function rollBack() {
  console.log('rolling back db to beginning state...');
  return knex.migrate.rollback().then(response => {
    if (response === 'none' || !response[0]) return Promise.resolve();
    return rollBack();
  });
}

function latest() {
  console.log('running knex migrations...');
  return knex.migrate.latest();
}

function execute(command) {
  return new Promise((resolve, reject) => {
    exec(command, (err, data) => {
      if (err) return reject(err);
      try {
        resolve(JSON.parse(data));
      } catch (_) {
        resolve(data);
      }
    });
  });
}

function getBucket(env) {
  switch (env) {
    case 'development':
      return 'photoapi.brenna.dev';
    case 'integration':
      return 'photoapi.brenna.int';
    default:
      console.log('Invalid Environment: "' + env + '"');
      process.exit(1);
  }
}
