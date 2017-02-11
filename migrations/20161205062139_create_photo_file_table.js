const findId = (array, uuid) => {
  let item = array.find(item => item.uuid === uuid);
  if (item) {
    return item.id || null;
  }
  return null;
};

exports.up = function(knex, Promise) {
  return knex.schema.raw('DROP TRIGGER before_insert_photos_uuid')
    .then(() => {
      return knex.schema.createTable('photo_files', photo_files => {
        photo_files.increments('id');
        photo_files.uuid('uuid').unique();
      });
    }).then(() => knex('photos').select('id', 'uuid'))
    .then(photos => {
      return knex.schema.table('photos', photos => {
        photos.dropColumn('uuid');
        photos.integer('photo_file_id')
          .unsigned()
          .references('id')
          .inTable('photo_files')
          .onDelete('CASCADE');
      }).then(() => {
        let sql = `CREATE TRIGGER before_insert_photo_files_uuid
                   BEFORE INSERT ON photo_files
                   FOR EACH ROW
                   BEGIN
                     IF new.uuid IS NULL THEN
                       SET new.uuid = uuid();
                     END IF;
                   END`;
        return knex.schema.raw(sql);
      }).then(() => Promise.all(photos.map(photo => {
        return knex('photo_files').returning('id').insert({ uuid: photo.uuid });
      }))).then(() => knex('photo_files'))
      .then(photoFiles => Promise.all(photoFiles.map(photoFile => {
        return knex('photos').update({ photo_file_id: photoFile.id })
          .where({ id: findId(photos, photoFile.uuid) });
      })));
    });
};

exports.down = function(knex, Promise) {
  return knex.schema.raw('DROP TRIGGER before_insert_photo_files_uuid')
    .then(() => {
      return knex('photos').select('photos.id as id', 'photo_files.uuid as uuid')
        .innerJoin('photo_files', 'photos.photo_file_id', 'photo_files.id');
    }).then(photoFiles => {
      return knex.schema.table('photos', photos => {
        photos.uuid('uuid').unique();
        photos.dropForeign('photo_file_id');
        photos.dropColumn('photo_file_id');
      }).then(() => knex.schema.dropTableIfExists('photo_files'))
      .then(() => {
        let sql = `CREATE TRIGGER before_insert_photos_uuid
                   BEFORE INSERT ON photos
                   FOR EACH ROW
                   BEGIN
                     IF new.uuid IS NULL THEN
                       SET new.uuid = uuid();
                     END IF;
                   END`;
        return knex.schema.raw(sql);
      }).then(() => Promise.all(photoFiles.map(photoFile => {
        return knex('photos').update({ uuid: photoFile.uuid }).where({ id: photoFile.id });
      })));
    });
};
