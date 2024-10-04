#Op1: Inserire un nuovo utente:
INSERT INTO utente(`email`, `nome`, `cognome`, `passw`, `tipo`, `num_telefono`, `cf`) 
VALUES ('travisscott@gmail.com', 'travis', 'Scott', 'TravisScott01', 1, '3398592677', 'TRVSCT92A54C163S');

#Op2: Inserire un nuovo brano o podcast ()
INSERT INTO contenuto(`nome`, `duarata`, `riproduzione`, `tipo`) 
VALUES ('ASTROWORLD', 415, 0, 0);

#Op3: Inserire un brano o podcast all'interno di una playlist
INSERT INTO contenuti_playlist(`idContenuto`, `nomePlaylist`, `email`) 
VALUES (1, 'Travis scott songs', 'lorenzopaoria@gmail.com');

#Op4: Ricerca di un brano o podcast per titolo
SELECT * FROM contenuto 
WHERE nome = 'ASTROWORLD';

#Op5: Visualizzare il numero di tracce di un album
INSERT INTO album(`nomeArtista`,`titolo`, `data_pubblicazione`, `num_tracce`)
VALUES ('Travis Scott', 'Rodeo', '2001/11/24', '12')

SELECT num_tracce
FROM album 
WHERE titolo = 'Rodeo' AND nomeArtista='Travis Scott';

#Op6: Visualizzare il numero di tracce di una playlist
INSERT INTO playlist(`email`,`nomePlaylist`, `num_tracce_P`)
VALUES ('lorenzopaoria@gmail.com', 'Travis scott songs', '1')

SELECT num_tracce_P
FROM playlist 
WHERE nomePlaylist='Travis scott songs';

#Op7: Visualizzare il numero di riproduzioni di un brano o podcast
SELECT riproduzione
FROM contenuto
WHERE idContenuto=(SELECT idContenuto
                   FROM Crea_Contenuto NATURAL JOIN contenuto
                   WHERE nomeArtista='Travis scott' AND nome='ASTROWORLD'
                   )

#Op8: Cambiare tipo di account da “free” a “premium”
INSERT INTO utente(`email`, `nome`, `cognome`, `passw`, `tipo`, `num_telefono`, `cf`, `stato`) 
VALUES ('lorenzopaoria@gmail.com', 'Lorenzo', 'Paoria', 'lori01', 0, '3398592454', 'PRALNZ01S24H163S', 1);

UPDATE utente
SET tipo=1
WHERE email="lorenzopaoria@gmail.com"

#Op9: Eliminare account
DELETE *
FROM utente 
WHERE email = 'lorenzopaoria@gmail.com';


