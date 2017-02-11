exports.up = function(knex, Promise) {
  return knex.schema.table('photos', photos => {
    photos.uuid('uuid').unique();
  }).then(() => {
    let sql = `CREATE TRIGGER before_insert_photos_uuid
               BEFORE INSERT ON photos
               FOR EACH ROW
               BEGIN
                 IF new.uuid IS NULL THEN
                   SET new.uuid = uuid();
                 END IF;
               END`;
    return knex.schema.raw(sql);
  });
};

exports.down = function(knex, Promise) {
  return knex.schema.raw('DROP TRIGGER before_insert_photos_uuid').then(() => {
    return knex.schema.table('photos', photos => {
      photos.dropColumn('uuid');
    });
  });
};
