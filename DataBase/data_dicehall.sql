START TRANSACTION;

--
-- Base de données : `dicehall`
--

USE `dicehall`;

-- Déchargement des données de la table `campaign`
INSERT INTO `campaign` (`id`, `name`, `privacy`) VALUES
(1, 'campagneDeFou', 0),
(2, 'campagneNulle', 1);

-- Déchargement des données de la table `rule`
INSERT INTO `rule` (`id`, `name`, `description`) VALUES
(1, 'règle1', 'description importante'),
(2, 'règle2', 'pas important'),
(3, 'règle3', 'un petit peu importante');

-- Déchargement des données de la table `campaign_rule`
INSERT INTO `campaign_rule` (`campaign_id`, `rule_id`) VALUES
(1, 1),
(1, 2),
(1, 3);

-- Déchargement des données de la table `user`
INSERT INTO `user` (`id`, `email`, `username`, `password`, `activecampaign_id`) VALUES
(1, 'toto@test.fr', 'toto', '$2a$10$zfNkDHq2HbPOOaFn6z2NwO4cdSNwQL6i9bCsncEO6m6fjSeeyjXIC', 1),
(2, 'tata@test.fr', 'tata', '$2a$10$KGIhkzv1PZ4VIdI54phDFO/Pr7GBNdBv2ue6Nx36ezFxlE0/DZ2Qy', 1),
(3, 'titi@test.fr', 'titi', '$2a$10$lshFk82enmmGomgn1NQDG.tFxNOyGIO9QUIJX0ddhBrYDazb0lAgy', 1);

-- Déchargement des données de la table `campaign_user`
INSERT INTO `campaign_user` (`campaign_id`, `user_id`, `role`) VALUES
(1, 1, 'Owner'),
(1, 2, 'Admin'),
(1, 3, 'Player'),
(2, 1, 'Owner');

-- Déchargement des données de la table `tag`
INSERT INTO `tag` (`id`, `name`) VALUES
(1, 'attention'),
(2, 'osef');

-- Déchargement des données de la table `rule_tag`
INSERT INTO `rule_tag` (`rule_id`, `tag_id`) VALUES
(1, 1),
(2, 2),
(3, 1);

-- Déchargement des données de la table `token`
INSERT INTO `token` (`value`, `user_id`, `created_at`) VALUES
('eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJEaWNlSGFsbCIsInN1YiI6IjEiLCJleHAiOjE4MjM5NjM3ODYsImlhdCI6MTc5MjQyNzc4Nn0.yD9SRMFD-pPQzGig0JL7hwJO_F7XkXRJ4ipoyByMSWQ', 1, '2026-10-19 18:36:26');


COMMIT;
