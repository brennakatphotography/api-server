exports.up = function(knex, Promise) {
  return knex.schema.createTable('user_roles', userRoles => {
    userRoles.increments('id');
    userRoles.string('email');
    userRoles.string('role');
  }).then(() => {
    return knex('user_roles').insert([
        { email: 'skuttleman@gmail.com', role: 'admin' },
        { email: 'bren04@gmail.com', role: 'power-user' }
    ]);
  });
};

exports.down = function(knex, Promise) {
  return knex.schema.dropTableIfExists('user_roles');
};
