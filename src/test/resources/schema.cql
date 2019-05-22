CREATE KEYSPACE sort WITH replication = {'class': 'NetworkTopologyStrategy', 'dc1': '1'}  AND durable_writes = true;

CREATE TABLE sort.sort_table (
    owner text,
    id text,
    name text,
    shortname text,
    PRIMARY KEY (owner, id)
);

INSERT INTO sort.sort_table(owner, id, name, shortname) VALUES ('owner', '1', 'Adam', 'Adamczyk');
INSERT INTO sort.sort_table(owner, id, name, shortname) VALUES ('owner', '2', 'Zorro', 'adamek');
INSERT INTO sort.sort_table(owner, id, name, shortname) VALUES ('owner', '3', 'adaś adaś', 'Adaś');
INSERT INTO sort.sort_table(owner, id, name, shortname) VALUES ('owner', '4', 'adaś adas', 'Adas');
INSERT INTO sort.sort_table(owner, id, name, shortname) VALUES ('owner', '5', 'zebra', 'zebra');
INSERT INTO sort.sort_table(owner, id, name, shortname) VALUES ('owner', '6', 'zebra zebra', 'Zebra');
INSERT INTO sort.sort_table(owner, id, name, shortname) VALUES ('owner', '7', '121', 'lorem ipsum cos tam cos tam');
INSERT INTO sort.sort_table(owner, id, name, shortname) VALUES ('owner', '8', '!12354', '! elo ! *YTRY%R@& asgvds gra o tron');
INSERT INTO sort.sort_table(owner, id, name, shortname) VALUES ('owner', '9', 'ądam', 'ądamczyk');
INSERT INTO sort.sort_table(owner, id, name, shortname) VALUES ('owner', '10', 'Adąm', 'Adamćzyk');