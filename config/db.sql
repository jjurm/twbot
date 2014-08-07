CREATE TABLE IF NOT EXISTS village (
	id INT UNSIGNED NOT NULL PRIMARY KEY,
	name VARCHAR(32) NOT NULL,
	x INT UNSIGNED NOT NULL,
	y INT UNSIGNED NOT NULL,
	player INT UNSIGNED NOT NULL,
	points INT UNSIGNED NOT NULL,
	rank INT UNSIGNED
);

CREATE TABLE IF NOT EXISTS player (
	id INT UNSIGNED NOT NULL PRIMARY KEY,
	name VARCHAR(24) NOT NULL,
	ally INT UNSIGNED,
	villages INT UNSIGNED NOT NULL,
	points INT UNSIGNED NOT NULL,
	rank INT UNSIGNED
);

CREATE TABLE IF NOT EXISTS ally (
	id INT UNSIGNED NOT NULL PRIMARY KEY,
	name VARCHAR(32) NOT NULL,
	tag VARCHAR(6) NOT NULL,
	members INT UNSIGNED NOT NULL,
	villages INT UNSIGNED NOT NULL,
	points INT UNSIGNED NOT NULL,
	all_points INT UNSIGNED NOT NULL,
	rank INT UNSIGNED NOT NULL
);

CREATE TABLE IF NOT EXISTS village_info (
	id INT UNSIGNED NOT NULL PRIMARY KEY,
	updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

	res_updated TIMESTAMP NULL DEFAULT NULL,
	wood INT UNSIGNED NULL DEFAULT NULL,
	stone INT UNSIGNED NULL DEFAULT NULL,
	iron INT UNSIGNED NULL DEFAULT NULL,

	prod_updated TIMESTAMP NULL DEFAULT NULL,
	wood_prod DOUBLE UNSIGNED NULL DEFAULT NULL,
	stone_prod DOUBLE UNSIGNED NULL DEFAULT NULL,
	iron_prod DOUBLE UNSIGNED NULL DEFAULT NULL,

	pop_updated TIMESTAMP NULL DEFAULT NULL,
	pop_total INT UNSIGNED NULL DEFAULT NULL,
	pop_used INT UNSIGNED NULL DEFAULT NULL,

	building_updated TIMESTAMP NULL DEFAULT NULL,
	building_main TINYINT UNSIGNED NULL DEFAULT NULL,
	building_barracks TINYINT UNSIGNED NULL DEFAULT NULL,
	building_stable TINYINT UNSIGNED NULL DEFAULT NULL,
	building_garage TINYINT UNSIGNED NULL DEFAULT NULL,
	building_church TINYINT UNSIGNED NULL DEFAULT NULL,
	building_church_f TINYINT UNSIGNED NULL DEFAULT NULL,
	building_snob TINYINT UNSIGNED NULL DEFAULT NULL,
	building_smith TINYINT UNSIGNED NULL DEFAULT NULL,
	building_place TINYINT UNSIGNED NULL DEFAULT NULL,
	building_statue TINYINT UNSIGNED NULL DEFAULT NULL,
	building_market TINYINT UNSIGNED NULL DEFAULT NULL,
	building_wood TINYINT UNSIGNED NULL DEFAULT NULL,
	building_stone TINYINT UNSIGNED NULL DEFAULT NULL,
	building_iron TINYINT UNSIGNED NULL DEFAULT NULL,
	building_farm TINYINT UNSIGNED NULL DEFAULT NULL,
	building_storage TINYINT UNSIGNED NULL DEFAULT NULL,
	building_hide TINYINT UNSIGNED NULL DEFAULT NULL,
	building_wall TINYINT UNSIGNED NULL DEFAULT NULL,

	unit_updated TIMESTAMP NULL DEFAULT NULL,
	unit_spear INT UNSIGNED NULL DEFAULT NULL,
	unit_sword INT UNSIGNED NULL DEFAULT NULL,
	unit_axe INT UNSIGNED NULL DEFAULT NULL,
	unit_archer INT UNSIGNED NULL DEFAULT NULL,
	unit_spy INT UNSIGNED NULL DEFAULT NULL,
	unit_light INT UNSIGNED NULL DEFAULT NULL,
	unit_marcher INT UNSIGNED NULL DEFAULT NULL,
	unit_heavy INT UNSIGNED NULL DEFAULT NULL,
	unit_ram INT UNSIGNED NULL DEFAULT NULL,
	unit_catapult INT UNSIGNED NULL DEFAULT NULL,
	unit_knight INT UNSIGNED NULL DEFAULT NULL,
	unit_snob INT UNSIGNED NULL DEFAULT NULL,

	unit_home_updated TIMESTAMP NULL DEFAULT NULL,
	unit_home_spear INT UNSIGNED NULL DEFAULT NULL,
	unit_home_sword INT UNSIGNED NULL DEFAULT NULL,
	unit_home_axe INT UNSIGNED NULL DEFAULT NULL,
	unit_home_archer INT UNSIGNED NULL DEFAULT NULL,
	unit_home_spy INT UNSIGNED NULL DEFAULT NULL,
	unit_home_light INT UNSIGNED NULL DEFAULT NULL,
	unit_home_marcher INT UNSIGNED NULL DEFAULT NULL,
	unit_home_heavy INT UNSIGNED NULL DEFAULT NULL,
	unit_home_ram INT UNSIGNED NULL DEFAULT NULL,
	unit_home_catapult INT UNSIGNED NULL DEFAULT NULL,
	unit_home_knight INT UNSIGNED NULL DEFAULT NULL,
	unit_home_snob INT UNSIGNED NULL DEFAULT NULL

);



DROP PROCEDURE IF EXISTS `update_res`;
CREATE PROCEDURE `update_res`(IN `xid` INT UNSIGNED, IN `xwood` INT UNSIGNED, IN `xstone` INT UNSIGNED, IN `xiron` INT UNSIGNED)
    MODIFIES SQL DATA
CALL update_res_timestamp(xid, NOW(), xwood, xstone, xiron);

DROP PROCEDURE IF EXISTS `update_res_timestamp`;
CREATE PROCEDURE `update_res_timestamp`(IN `xid` INT UNSIGNED, IN `xtime` TIMESTAMP, IN `xwood` INT UNSIGNED, IN `xstone` INT UNSIGNED, IN `xiron` INT UNSIGNED)
    MODIFIES SQL DATA
INSERT INTO village_info (id, res_updated, wood, stone, iron) VALUES (xid, xtime, xwood, xstone, xiron) ON DUPLICATE KEY UPDATE res_updated = xtime, wood = xwood, stone = xstone, iron = xiron;



DROP PROCEDURE IF EXISTS `update_prod`;
CREATE PROCEDURE `update_prod`(IN `xid` INT UNSIGNED, IN `xwood_prod` DOUBLE UNSIGNED, IN `xstone_prod` DOUBLE UNSIGNED, IN `xiron_prod` DOUBLE UNSIGNED)
    MODIFIES SQL DATA
CALL update_prod_timestamp(xid, NOW(), xwood_prod, xstone_prod, xiron_prod);

DROP PROCEDURE IF EXISTS `update_prod_timestamp`;
CREATE PROCEDURE `update_prod_timestamp`(IN `xid` INT UNSIGNED, IN `xtime` TIMESTAMP, IN `xwood_prod` DOUBLE UNSIGNED, IN `xstone_prod` DOUBLE UNSIGNED, IN `xiron_prod` DOUBLE UNSIGNED)
    MODIFIES SQL DATA
INSERT INTO village_info (id, prod_updated, wood_prod, stone_prod, iron_prod) VALUES (xid, xtime, xwood_prod, xstone_prod, xiron_prod) ON DUPLICATE KEY UPDATE prod_updated = xtime, wood_prod = xwood_prod, stone_prod = xstone_prod, iron_prod = xiron_prod;



DROP PROCEDURE IF EXISTS `update_pop`;
CREATE PROCEDURE `update_pop`(IN `xid` INT UNSIGNED, IN `xpop_total` INT UNSIGNED, IN `xpop_used` INT UNSIGNED)
    MODIFIES SQL DATA
CALL update_pop_timestamp(xid, NOW(), xpop_total, xpop_used);

DROP PROCEDURE IF EXISTS `update_pop_timestamp`;
CREATE PROCEDURE `update_pop_timestamp`(IN `xid` INT UNSIGNED, IN `xtime` TIMESTAMP, IN `xpop_total` INT UNSIGNED, IN `xpop_used` INT UNSIGNED)
    MODIFIES SQL DATA
INSERT INTO village_info (id, pop_updated, pop_total, pop_used) VALUES (xid, xtime, xpop_total, xpop_used) ON DUPLICATE KEY UPDATE pop_updated = xtime, pop_total = xpop_total, pop_used = xpop_used;



DROP PROCEDURE IF EXISTS `update_building`;
CREATE PROCEDURE `update_building`(IN `xid` INT UNSIGNED, IN `xbuilding_main` TINYINT UNSIGNED, IN `xbuilding_barracks` TINYINT UNSIGNED, IN `xbuilding_stable` TINYINT UNSIGNED, IN `xbuilding_garage` TINYINT UNSIGNED, IN `xbuilding_church` TINYINT UNSIGNED, IN `xbuilding_church_f` TINYINT UNSIGNED, IN `xbuilding_snob` TINYINT UNSIGNED, IN `xbuilding_smith` TINYINT UNSIGNED, IN `xbuilding_place` TINYINT UNSIGNED, IN `xbuilding_statue` TINYINT UNSIGNED, IN `xbuilding_market` TINYINT UNSIGNED, IN `xbuilding_wood` TINYINT UNSIGNED, IN `xbuilding_stone` TINYINT UNSIGNED, IN `xbuilding_iron` TINYINT UNSIGNED, IN `xbuilding_farm` TINYINT UNSIGNED, IN `xbuilding_storage` TINYINT UNSIGNED, IN `xbuilding_hide` TINYINT UNSIGNED, IN `xbuilding_wall` TINYINT UNSIGNED)
    MODIFIES SQL DATA
CALL update_building_timestamp(xid, NOW(), xbuilding_main, xbuilding_barracks, xbuilding_stable, xbuilding_garage, xbuilding_church, xbuilding_church_f, xbuilding_snob, xbuilding_smith, xbuilding_place, xbuilding_statue, xbuilding_market, xbuilding_wood, xbuilding_stone, xbuilding_iron, xbuilding_farm, xbuilding_storage, xbuilding_hide, xbuilding_wall);

DROP PROCEDURE IF EXISTS `update_building_timestamp`;
CREATE PROCEDURE `update_building_timestamp`(IN `xid` INT UNSIGNED, IN `xtime` TIMESTAMP, IN `xbuilding_main` TINYINT UNSIGNED, IN `xbuilding_barracks` TINYINT UNSIGNED, IN `xbuilding_stable` TINYINT UNSIGNED, IN `xbuilding_garage` TINYINT UNSIGNED, IN `xbuilding_church` TINYINT UNSIGNED, IN `xbuilding_church_f` TINYINT UNSIGNED, IN `xbuilding_snob` TINYINT UNSIGNED, IN `xbuilding_smith` TINYINT UNSIGNED, IN `xbuilding_place` TINYINT UNSIGNED, IN `xbuilding_statue` TINYINT UNSIGNED, IN `xbuilding_market` TINYINT UNSIGNED, IN `xbuilding_wood` TINYINT UNSIGNED, IN `xbuilding_stone` TINYINT UNSIGNED, IN `xbuilding_iron` TINYINT UNSIGNED, IN `xbuilding_farm` TINYINT UNSIGNED, IN `xbuilding_storage` TINYINT UNSIGNED, IN `xbuilding_hide` TINYINT UNSIGNED, IN `xbuilding_wall` TINYINT UNSIGNED)
    MODIFIES SQL DATA
INSERT INTO village_info (id, building_updated, building_main, building_barracks, building_stable, building_garage, building_church, building_church_f, building_snob, building_smith, building_place, building_statue, building_market, building_wood, building_stone, building_iron, building_farm, building_storage, building_hide, building_wall) VALUES (xid, xtime, xbuilding_main, xbuilding_barracks, xbuilding_stable, xbuilding_garage, xbuilding_church, xbuilding_church_f, xbuilding_snob, xbuilding_smith, xbuilding_place, xbuilding_statue, xbuilding_market, xbuilding_wood, xbuilding_stone, xbuilding_iron, xbuilding_farm, xbuilding_storage, xbuilding_hide, xbuilding_wall) ON DUPLICATE KEY UPDATE building_updated = xtime, building_main = xbuilding_main, building_barracks = xbuilding_barracks, building_stable = xbuilding_stable, building_garage = xbuilding_garage, building_church = xbuilding_church, building_church_f = xbuilding_church_f, building_snob = xbuilding_snob, building_smith = xbuilding_smith, building_place = xbuilding_place, building_statue = xbuilding_statue, building_market = xbuilding_market, building_wood = xbuilding_wood, building_stone = xbuilding_stone, building_iron = xbuilding_iron, building_farm = xbuilding_farm, building_storage = xbuilding_storage, building_hide = xbuilding_hide, building_wall = xbuilding_wall;



DROP PROCEDURE IF EXISTS `update_unit`;
CREATE PROCEDURE `update_unit`(IN `xid` INT UNSIGNED, IN `xunit_spear` INT UNSIGNED, IN `xunit_sword` INT UNSIGNED, IN `xunit_axe` INT UNSIGNED, IN `xunit_archer` INT UNSIGNED, IN `xunit_spy` INT UNSIGNED, IN `xunit_light` INT UNSIGNED, IN `xunit_marcher` INT UNSIGNED, IN `xunit_heavy` INT UNSIGNED, IN `xunit_ram` INT UNSIGNED, IN `xunit_catapult` INT UNSIGNED, IN `xunit_knight` INT UNSIGNED, IN `xunit_snob` INT UNSIGNED)
    MODIFIES SQL DATA
CALL update_unit_timestamp(xid, NOW(), xunit_spear, xunit_sword, xunit_axe, xunit_archer, xunit_spy, xunit_light, xunit_marcher, xunit_heavy, xunit_ram, xunit_catapult, xunit_knight, xunit_snob);

DROP PROCEDURE IF EXISTS `update_unit_timestamp`;
CREATE PROCEDURE `update_unit_timestamp`(IN `xid` INT UNSIGNED, IN `xtime` TIMESTAMP, IN `xunit_spear` INT UNSIGNED, IN `xunit_sword` INT UNSIGNED, IN `xunit_axe` INT UNSIGNED, IN `xunit_archer` INT UNSIGNED, IN `xunit_spy` INT UNSIGNED, IN `xunit_light` INT UNSIGNED, IN `xunit_marcher` INT UNSIGNED, IN `xunit_heavy` INT UNSIGNED, IN `xunit_ram` INT UNSIGNED, IN `xunit_catapult` INT UNSIGNED, IN `xunit_knight` INT UNSIGNED, IN `xunit_snob` INT UNSIGNED)
    MODIFIES SQL DATA
INSERT INTO village_info (id, unit_updated, unit_spear, unit_sword, unit_axe, unit_archer, unit_spy, unit_light, unit_marcher, unit_heavy, unit_ram, unit_catapult, unit_knight, unit_snob) VALUES (xid, xtime, xunit_spear, xunit_sword, xunit_axe, xunit_archer, xunit_spy, xunit_light, xunit_marcher, xunit_heavy, xunit_ram, xunit_catapult, xunit_knight, xunit_snob) ON DUPLICATE KEY UPDATE unit_updated = xtime, unit_spear = xunit_spear, unit_sword = xunit_sword, unit_axe = xunit_axe, unit_archer = xunit_archer, unit_spy = xunit_spy, unit_light = xunit_light, unit_marcher = xunit_marcher, unit_heavy = xunit_heavy, unit_ram = xunit_ram, unit_catapult = xunit_catapult, unit_knight = xunit_knight, unit_snob = xunit_snob;



DROP PROCEDURE IF EXISTS `update_unit_home`;
CREATE PROCEDURE `update_unit_home`(IN `xid` INT UNSIGNED, IN `xunit_home_spear` INT UNSIGNED, IN `xunit_home_sword` INT UNSIGNED, IN `xunit_home_axe` INT UNSIGNED, IN `xunit_home_archer` INT UNSIGNED, IN `xunit_home_spy` INT UNSIGNED, IN `xunit_home_light` INT UNSIGNED, IN `xunit_home_marcher` INT UNSIGNED, IN `xunit_home_heavy` INT UNSIGNED, IN `xunit_home_ram` INT UNSIGNED, IN `xunit_home_catapult` INT UNSIGNED, IN `xunit_home_knight` INT UNSIGNED, IN `xunit_home_snob` INT UNSIGNED)
    MODIFIES SQL DATA
CALL update_unit_home_timestamp(xid, NOW(), xunit_home_spear, xunit_home_sword, xunit_home_axe, xunit_home_archer, xunit_home_spy, xunit_home_light, xunit_home_marcher, xunit_home_heavy, xunit_home_ram, xunit_home_catapult, xunit_home_knight, xunit_home_snob);

DROP PROCEDURE IF EXISTS `update_unit_home_timestamp`;
CREATE PROCEDURE `update_unit_home_timestamp`(IN `xid` INT UNSIGNED, IN `xtime` TIMESTAMP, IN `xunit_home_spear` INT UNSIGNED, IN `xunit_home_sword` INT UNSIGNED, IN `xunit_home_axe` INT UNSIGNED, IN `xunit_home_archer` INT UNSIGNED, IN `xunit_home_spy` INT UNSIGNED, IN `xunit_home_light` INT UNSIGNED, IN `xunit_home_marcher` INT UNSIGNED, IN `xunit_home_heavy` INT UNSIGNED, IN `xunit_home_ram` INT UNSIGNED, IN `xunit_home_catapult` INT UNSIGNED, IN `xunit_home_knight` INT UNSIGNED, IN `xunit_home_snob` INT UNSIGNED)
    MODIFIES SQL DATA
INSERT INTO village_info (id, unit_home_updated, unit_home_spear, unit_home_sword, unit_home_axe, unit_home_archer, unit_home_spy, unit_home_light, unit_home_marcher, unit_home_heavy, unit_home_ram, unit_home_catapult, unit_home_knight, unit_home_snob) VALUES (xid, xtime, xunit_home_spear, xunit_home_sword, xunit_home_axe, xunit_home_archer, xunit_home_spy, xunit_home_light, xunit_home_marcher, xunit_home_heavy, xunit_home_ram, xunit_home_catapult, xunit_home_knight, xunit_home_snob) ON DUPLICATE KEY UPDATE unit_home_updated = xtime, unit_home_spear = xunit_home_spear, unit_home_sword = xunit_home_sword, unit_home_axe = xunit_home_axe, unit_home_archer = xunit_home_archer, unit_home_spy = xunit_home_spy, unit_home_light = xunit_home_light, unit_home_marcher = xunit_home_marcher, unit_home_heavy = xunit_home_heavy, unit_home_ram = xunit_home_ram, unit_home_catapult = xunit_home_catapult, unit_home_knight = xunit_home_knight, unit_home_snob = xunit_home_snob;







CREATE TABLE IF NOT EXISTS MassAttackSender (
	id INT UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
	active INT(1) NOT NULL DEFAULT 1,
	priority INT NOT NULL DEFAULT 0,
	start INT UNSIGNED NOT NULL,
	target INT UNSIGNED NOT NULL,
	cycle_max INT NOT NULL DEFAULT -1,
	total_max INT NOT NULL DEFAULT -1,
	unit_spear INT UNSIGNED NOT NULL DEFAULT 0,
	unit_sword INT UNSIGNED NOT NULL DEFAULT 0,
	unit_axe INT UNSIGNED NOT NULL DEFAULT 0,
	unit_archer INT UNSIGNED NOT NULL DEFAULT 0,
	unit_spy INT UNSIGNED NOT NULL DEFAULT 0,
	unit_light INT UNSIGNED NOT NULL DEFAULT 0,
	unit_marcher INT UNSIGNED NOT NULL DEFAULT 0,
	unit_heavy INT UNSIGNED NOT NULL DEFAULT 0,
	unit_ram INT UNSIGNED NOT NULL DEFAULT 0,
	unit_catapult INT UNSIGNED NOT NULL DEFAULT 0,
	unit_knight INT UNSIGNED NOT NULL DEFAULT 0,
	unit_snob INT UNSIGNED NOT NULL DEFAULT 0
);

