CREATE TABLE users (
  id INT NOT NULL,
  name VARCHAR(50) NOT NULL,
  pass VARCHAR(255) NOT NULL,
  role VARCHAR(20) NOT NULL,
  PRIMARY KEY(id)
);

CREATE TABLE auths (
  id INT NOT NULL,
  userId INT NOT NULL,
  token VARCHAR(255) NOT NULL,
  creationDate DATE NOT NULL,
  expirationDate DATE NOT NULL,
  PRIMARY KEY(id),
  FOREIGN KEY(userId) REFERENCES users(id)
);

CREATE TABLE restaurants (
  id INT NOT NULL,
  name VARCHAR(255) NOT NULL,
  address_street VARCHAR(255),
  address_neighborhood VARCHAR(255),
  address_city VARCHAR(255),
  address_state VARCHAR(255),
  address_country VARCHAR(255),
  address_zip VARCHAR(255),
  geoLocalization_latitude DOUBLE NOT NULL,
  geoLocalization_longitude DOUBLE NOT NULL,
  PRIMARY KEY(id)
);