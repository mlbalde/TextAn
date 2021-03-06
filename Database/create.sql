-- creates a database schema 'textan' and all necessary tables in it

CREATE DATABASE `textan`
-- IF NOT EXISTS
  DEFAULT CHARACTER SET utf8
  DEFAULT COLLATE utf8_general_ci;
USE textan;


CREATE TABLE GlobalVersion (
  id_global_version int PRIMARY KEY AUTO_INCREMENT,
  version int DEFAULT 0 NOT NULL
);

INSERT INTO  `GlobalVersion` (  `version` )
VALUES ( 0 );

CREATE TABLE Audit (
	id_audit int PRIMARY KEY AUTO_INCREMENT, 
  username NVARCHAR(255) NOT NULL,
  edit_date datetime NOT NULL,
  -- INSERT | DELETE | UPDATE
  edittype VARCHAR(255) NOT NULL,
  edit text CHARSET utf8 NOT NULL
);


CREATE TABLE Document (
	id_document int PRIMARY KEY AUTO_INCREMENT, 
	added datetime NOT NULL,
	last_change datetime NOT NULL,
	processed datetime NULL,
	text text CHARSET utf8 NOT NULL,
	globalversion int DEFAULT 0 NOT NULL
);

CREATE TABLE RelationType(
  id_relation_type INT PRIMARY KEY AUTO_INCREMENT,
  name NVARCHAR (255) UNIQUE
);

CREATE TABLE ObjectType (
	id_object_type int PRIMARY KEY AUTO_INCREMENT, 
	name NVARCHAR (255) UNIQUE
);

CREATE TABLE Object (
	id_object int PRIMARY KEY AUTO_INCREMENT, 
	id_object_type int NOT NULL,
	data NVARCHAR (255),
    CONSTRAINT FK_OBJECT_TO_TYPE FOREIGN KEY (id_object_type)
  		REFERENCES ObjectType(id_object_type)
      ON DELETE CASCADE,
	globalversion int DEFAULT 0 NOT NULL,
    id_root_object int
);

CREATE TABLE Alias (
	id_alias int PRIMARY KEY AUTO_INCREMENT, 
	id_object int NOT NULL,
	alias NVARCHAR(255) NOT NULL,
  CONSTRAINT FK_ALIAS_ID_OBJECT
   FOREIGN KEY (id_object)
		REFERENCES Object(id_object)
    ON DELETE CASCADE
);


CREATE TABLE AliasOccurrence (
	id_alias_occurrence int PRIMARY KEY AUTO_INCREMENT,
	id_alias int NOT NULL, 
	id_document int NOT NULL, 
	position int NOT NULL,
  CONSTRAINT FK_ALIASOCCURRENCE_IDALIAS
   FOREIGN KEY (id_alias)
		REFERENCES Alias(id_alias)
    ON DELETE CASCADE,
  CONSTRAINT FK_ALIASOCCURRENCE_IDDOCUMENT
   FOREIGN KEY (id_document)
		REFERENCES Document(id_document)
    ON DELETE CASCADE
);

CREATE TABLE Relation
(
        id_relation int PRIMARY KEY AUTO_INCREMENT,
        id_relation_type int NOT NULL,
        CONSTRAINT FK_RELATION_RELTYPE FOREIGN KEY (id_relation_type)
                REFERENCES RelationType (id_relation_type)
                ON DELETE CASCADE,
	      globalversion int DEFAULT 0 NOT NULL
);

CREATE TABLE IsInRelation
(
        id_is_in_relation int PRIMARY KEY AUTO_INCREMENT,
        id_relation int NOT NULL,
        CONSTRAINT FK_ISINRELATION_RELATION
          FOREIGN KEY (id_relation)
                REFERENCES Relation(id_relation)
                ON DELETE CASCADE,
        id_object int NOT NULL,
        CONSTRAINT FK_ISINRELATION_OBJECT
          FOREIGN KEY (id_object)
                REFERENCES Object(id_object)
                ON DELETE CASCADE,
        role NVARCHAR(255),
        order_in_relation int NOT NULL
);

CREATE TABLE JoinedObjects
(       
        id_joined_object int PRIMARY KEY AUTO_INCREMENT,
        id_new_object int NOT NULL,
          CONSTRAINT FK_PK_JOINEDOBJECTS_ID
            FOREIGN KEY (id_new_object)
                  REFERENCES Object(id_object)
                  ON DELETE CASCADE, 
        id_old_object1 int NOT NULL,
          CONSTRAINT FK_JOINEDOBJECTS_OLDOBJ1
            FOREIGN KEY (id_old_object1)
                  REFERENCES Object(id_object)
                  ON DELETE CASCADE, 
        id_old_object2 int NOT NULL,
          CONSTRAINT FK_JOINEDOBJECTS_OLDOBJ2
            FOREIGN KEY (id_old_object2)
                  REFERENCES Object(id_object)
                  ON DELETE CASCADE, 
        from_date datetime NOT NULL,
        to_date datetime,
	      globalversion int DEFAULT 0 NOT NULL
);

CREATE TABLE RelationOccurrence
(

        id_relation_occurrence int PRIMARY KEY AUTO_INCREMENT,
        id_relation int NOT NULL,
        CONSTRAINT FK_RELOCCURRENCE_RELATION
          FOREIGN KEY (id_relation)
            REFERENCES Relation(id_relation)
            ON DELETE CASCADE,
        id_document int NOT NULL,
        CONSTRAINT FK_RELOCCURRENCE_DOCUMENT
          FOREIGN KEY (id_document)
                REFERENCES Document(id_document)
                ON DELETE CASCADE,
        position int,
        anchor NVARCHAR(255)
);
