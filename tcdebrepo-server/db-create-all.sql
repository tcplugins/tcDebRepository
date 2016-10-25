create table o_customer (
  id                            bigint auto_increment not null,
  name                          varchar(255),
  registered                    timestamp,
  comments                      varchar(255),
  constraint pk_o_customer primary key (id)
);

