const fs = require('fs');

const readContents = () => {
  return String(fs.readFileSync(__dirname + '/../.lein-env')).split('\n');
};

const trim = string => string.trim();

const filterString = string => string && !string.match(/[{}]/) && !string.match(/^;/);

const splitLine = line => {
  let preComment = line.split(';')[0];
  return trim(preComment).split(/("[^"]+"|\s?\w+?\s)/).map(trim);
};

const pairUp = () => {
  return readContents().reduce((lines, line) => {
    let newPair = splitLine(line).filter(filterString);
    if (newPair.length) {
      return lines.concat([newPair]);
    }
    return lines;
  }, []);
};

const addEnv = pairs => {
  pairs.forEach(pair => {
    let [key, value] = pair.map(JSON.parse);
    process.env[key] = value ? value : '';
  });
};

addEnv(pairUp());
