Testovanie komunikácie pomocou kniznice jserialcomm a
zariadenia arduino, ktoré imituje čip krokového motoru.

*pripojené Arduino vysiela neustále znak # na port na ktorý je pripojený
*pomocou jserialcomm library cyklom prebehneme aktivne porty
*ak nájdeme hľadaný port na ktorom mame pripojene zariadenie(porovnáme názvy)
	- ak sa toto zariadenie nenachádza v zozname tak vypíše chybovú hlášku
	- ak sa nachádza tak si ho uložíme a otvoríme port
	  následne budeme odchytávať komunikaciu na ňom v podobe znakov '#',
	  ktoré hovoria o tom, že komunikuje.

Ak s ním komunikujeme môžeme mu poslať znak:
	- znak '!' a on zastaví posielanie znakov 
	  a začne akceptovať znaky '+' a '-'.
	- znak '!'inak tento znak vykoná PING - ak sme pripojený,
          zariadenie nám odošle 1 znak '#' ako dôkaz, že sme pripojený
	- znaky '+' a '-' príjma (v prípade krokového motora,
	  vykonáva pohyby v určitom / opačnom smere o 1 krok)