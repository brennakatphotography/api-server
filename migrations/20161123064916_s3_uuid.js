
exports.up = function(knex, Promise) {
  return knex.schema.table('photos', table => {
    table.uuid('uuid').unique();
  }).then(() => {
    return knex.schema.raw(`CREATE TRIGGER before_insert_photos_uuid
                            BEFORE INSERT ON photos
                            FOR EACH ROW
                            BEGIN
                              IF new.uuid IS NULL THEN
                                SET new.uuid = uuid();
                              END IF;
                            END`);
  });
};

exports.down = function(knex, Promise) {
  return knex.schema.raw('DROP TRIGGER before_insert_photos_uuid').then(() => {
    return knex.schema.table('photos', table => {
      table.dropColumn('uuid');
    });
  });
};
