START TRANSACTION;

--
-- Base de données : `dicehall`
--

CREATE DATABASE IF NOT EXISTS `dicehall`;
USE `dicehall`;

--
-- Structures des tables
--

-- Structure de la table `campaign`
CREATE TABLE `campaign` (
  `id` int(11) NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `name` varchar(64) NOT NULL,
  `ruleset_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Structure de la table `campaign_user`
CREATE TABLE `campaign_user` (
  `campaign_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `role` enum('Owner','Admin','Player') NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Structure de la table `character`
CREATE TABLE `character` (
  `id` int(11) NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `name` varchar(64) NOT NULL,
  `user_id` int(11) NOT NULL,
  `campaign_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Structure de la table `character_stat`
CREATE TABLE `character_stat` (
  `owner_id` int(11) NOT NULL,
  `stat_id` int(11) NOT NULL,
  `value` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Structure de la table `item`
CREATE TABLE `item` (
  `id` int(11) NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `name` varchar(64) NOT NULL,
  `campaign_id` int(11) NOT NULL,
  `owner_id` int(11) DEFAULT NULL,
  `slot` varchar(32) NOT NULL,
  `description` varchar(256) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Structure de la table `rule`
CREATE TABLE `rule` (
  `id` int(11) NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `ruleset_id` int(11) NOT NULL,
  `name` varchar(64) DEFAULT NULL,
  `description` varchar(1024) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Structure de la table `ruleset`
CREATE TABLE `ruleset` (
  `id` int(11) NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `name` varchar(64) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Structure de la table `rule_tag`
CREATE TABLE `rule_tag` (
  `rule_id` int(11) NOT NULL,
  `tag_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Structure de la table `stat`
CREATE TABLE `stat` (
  `id` int(11) NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `name` varchar(64) NOT NULL,
  `ruleset_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Structure de la table `tag`
CREATE TABLE `tag` (
  `id` int(11) NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `name` varchar(64) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Structure de la table `token`
CREATE TABLE `token` (
  `value` varchar(256) NOT NULL PRIMARY KEY,
  `user_id` int(11) NOT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Structure de la table `user`
CREATE TABLE `user` (
  `id` int(11) NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `email` varchar(64) NOT NULL UNIQUE KEY,
  `username` varchar(64) NOT NULL,
  `password` varchar(64) DEFAULT NULL,
  `activecampaign_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


--
-- Index pour les tables
--

-- Index pour la table `campaign`
ALTER TABLE `campaign`
  ADD KEY `campaign_ruleset_id` (`ruleset_id`);

-- Index pour la table `campaign_user`
ALTER TABLE `campaign_user`
  ADD PRIMARY KEY (`campaign_id`,`user_id`),
  ADD KEY `user_campaign_id` (`user_id`);

-- Index pour la table `character`
ALTER TABLE `character`
  ADD KEY `character_user_id` (`user_id`),
  ADD KEY `character_campaign_id` (`campaign_id`);

-- Index pour la table `character_stat`
ALTER TABLE `character_stat`
  ADD PRIMARY KEY (`owner_id`,`stat_id`),
  ADD KEY `character_stat_id` (`stat_id`);

-- Index pour la table `item`
ALTER TABLE `item`
  ADD KEY `item_campaign_id` (`campaign_id`),
  ADD KEY `item_owner_id` (`owner_id`);

-- Index pour la table `rule`
ALTER TABLE `rule`
  ADD KEY `rule_ruleset_id` (`ruleset_id`);

-- Index pour la table `rule_tag`
ALTER TABLE `rule_tag`
  ADD PRIMARY KEY (`rule_id`,`tag_id`),
  ADD KEY `rule_tag_id` (`tag_id`);

-- Index pour la table `stat`
ALTER TABLE `stat`
  ADD KEY `stat_ruleset_id` (`ruleset_id`);

-- Index pour la table `token`
ALTER TABLE `token`
  ADD KEY `token_user_id` (`user_id`);

-- Index pour la table `user`
ALTER TABLE `user`
  ADD KEY `user_activecampaign_id` (`activecampaign_id`);


--
-- Contraintes pour les tables
--

-- Contraintes pour la table `campaign`
ALTER TABLE `campaign`
  ADD CONSTRAINT `campaign_ruleset_id` FOREIGN KEY (`ruleset_id`) REFERENCES `ruleset` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

-- Contraintes pour la table `campaign_user`
ALTER TABLE `campaign_user`
  ADD CONSTRAINT `campaign_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `user_campaign_id` FOREIGN KEY (`campaign_id`) REFERENCES `campaign` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

-- Contraintes pour la table `character`
ALTER TABLE `character`
  ADD CONSTRAINT `character_campaign_id` FOREIGN KEY (`campaign_id`) REFERENCES `campaign` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `character_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

-- Contraintes pour la table `character_stat`
ALTER TABLE `character_stat`
  ADD CONSTRAINT `stat_character_id` FOREIGN KEY (`owner_id`) REFERENCES `character` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `character_stat_id` FOREIGN KEY (`stat_id`) REFERENCES `stat` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

-- Contraintes pour la table `item`
ALTER TABLE `item`
  ADD CONSTRAINT `item_campaign_id` FOREIGN KEY (`campaign_id`) REFERENCES `campaign` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `item_owner_id` FOREIGN KEY (`owner_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

-- Contraintes pour la table `rule`
ALTER TABLE `rule`
  ADD CONSTRAINT `rule_ruleset_id` FOREIGN KEY (`ruleset_id`) REFERENCES `ruleset` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

-- Contraintes pour la table `rule_tag`
ALTER TABLE `rule_tag`
  ADD CONSTRAINT `rule_tag_id` FOREIGN KEY (`tag_id`) REFERENCES `tag` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `tag_rule_id` FOREIGN KEY (`rule_id`) REFERENCES `rule` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

-- Contraintes pour la table `stat`
ALTER TABLE `stat`
  ADD CONSTRAINT `stat_ruleset_id` FOREIGN KEY (`ruleset_id`) REFERENCES `ruleset` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

-- Contraintes pour la table `token`
ALTER TABLE `token`
  ADD CONSTRAINT `token_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

-- Contraintes pour la table `user`
ALTER TABLE `user`
  ADD CONSTRAINT `user_activecampaign_id` FOREIGN KEY (`activecampaign_id`) REFERENCES `campaign` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;


COMMIT;
