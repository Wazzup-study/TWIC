package com.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.springframework.stereotype.Repository;

import com.config.JDBCConfiguration;
import com.dto.Ville;

@Repository
public class VilleDAOImpl implements VilleDAO {

	private static Logger logger = Logger.getLogger(VilleDAOImpl.class.getName());

	private static final String[] ATTRIBUTS_VILLE = { "Code_commune_INSEE", "Nom_commune", "Code_postal", "Latitude",
			"Longitude", "Libelle_acheminement", "Ligne_5" };

	public ArrayList<Ville> findAllVilles() {
		String requete = "SELECT * FROM ville_france";
		ArrayList<Ville> villes = getVilleWithRequete(requete);
		return villes;
	}

	@Override
	public ArrayList<Ville> findSomeVilles(Ville ville) {
		String requete = "SELECT * FROM ville_france";
		String requeteWhere = " WHERE ";
		String requeteFinale = requeteBuilder(ville, requete, requeteWhere);

		ArrayList<Ville> villes = getVilleWithRequete(requeteFinale);

		return villes;
	}


	@Override
	public boolean addVille(Ville ville) {

		String requete = "INSERT INTO ville_france (";

		for (String attribut : ATTRIBUTS_VILLE) {
			requete += "" + attribut + ",";
		}
		requete = requete.substring(0, requete.length() - 1);
		requete += ") VALUES (";

		if (ville.getCodeCommune() != null) {
			requete += "'" + ville.getCodeCommune() + "',";
		} else {
			requete += "'NULL" + Math.round(10000 * Math.random()) + "',"; // En tant que clef primaire on ne peut pas
																			// donner la même valeur à chaque fois.
		}

		requete += checkNull(ville.getNomCommune());
		requete += checkNull(ville.getCodePostal());
		requete += checkNull(ville.getLatitude());
		requete += checkNull(ville.getLongitude());
		requete += checkNull(ville.getLibelleAcheminement());
		requete += checkNull(ville.getLigne_5());

		requete = requete.substring(0, requete.length() - 1);
		requete += ")";

		return executeRequete(requete);
	}

	// Modifie la ville dont le numero de commune est donné dans la ville en
	// parametre et lui donnes les autres parametres donnés dans la ville.

	@Override
	public boolean modifVille(Ville ville) {
		String requete = "UPDATE ville_france SET ";
		String requeteWhere = " WHERE Code_commune_INSEE = '" + ville.getCodeCommune() + "'";

		String[][] attributs = { ATTRIBUTS_VILLE,
				{ ville.getCodeCommune(), ville.getNomCommune(), ville.getCodePostal(), ville.getLatitude(),
						ville.getLongitude(), ville.getLibelleAcheminement(), ville.getLigne_5() } };

		boolean conditionSQL = false;
		for (int i = 1; i < attributs[0].length; i++) {
			if (attributs[1][i] != null) {
				if (conditionSQL) {
					requete += ", ";
				}
				requete += attributs[0][i] + "='" + attributs[1][i] + "'";
				conditionSQL = true;
			}
		}

		return executeRequete(requete + requeteWhere);
	}

	// DELETE toutes les villes dont les caracteristiques correspondent à celle
	// donnée en paramètre

	@Override
	public boolean deleteVille(Ville ville) {
		String requete = "DELETE FROM ville_france";
		String requeteWhere = " WHERE ";

		String requeteFinale = requeteBuilder(ville, requete, requeteWhere);

		return executeRequete(requeteFinale);

	}

	@SuppressWarnings("deprecation")
	private ArrayList<Ville> getVilleWithRequete(String requete) {
		ArrayList<Ville> villes = new ArrayList<Ville>();
		Connection con = JDBCConfiguration.getConnection();

		try (Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery(requete);) {
			while (rs.next()) {
				Ville ville = new Ville();
				ville.setCodeCommune(rs.getString("code_commune_insee"));
				ville.setNomCommune(rs.getString("nom_commune"));
				ville.setCodePostal(rs.getString("code_postal"));
				ville.setLatitude(rs.getString("latitude"));
				ville.setLongitude(rs.getString("longitude"));
				ville.setLibelleAcheminement(rs.getString("libelle_acheminement"));
				ville.setLigne_5(rs.getString("ligne_5"));
				villes.add(ville);
			}
		} catch (SQLException e) {
			logger.log(Priority.ERROR, "Error, couldn't get ville.");
		} finally {
			try {
				con.close();
			} catch (SQLException e) {
				logger.log(Priority.ERROR, "Error, couldn't close con1.");
			}
		}
		return villes;
	}

	@SuppressWarnings("deprecation")
	private boolean executeRequete(String requete) {
		boolean booleanRequete;
		Connection con = JDBCConfiguration.getConnection();
		try (Statement stmt = con.createStatement();) {
			stmt.executeUpdate(requete);
			booleanRequete = true;
		} catch (SQLException e) {
			booleanRequete = false;
			logger.log(Priority.ERROR, "");
		} finally {
			try {
				con.close();
			} catch (SQLException e) {
				logger.log(Priority.ERROR, "Error, couldn't close con.");
			}
		}
		return booleanRequete;
	}

	private String checkNull(String string) {
		if (string != null) {
			return "'" + string + "',";
		} else {
			return "'NULL',";
		}

	}
	
	private String requeteBuilder(Ville ville, String requete, String requeteWhere) {
		String[][] attributs = { ATTRIBUTS_VILLE,
				{ ville.getCodeCommune(), ville.getNomCommune(), ville.getCodePostal(), ville.getLatitude(),
						ville.getLongitude(), ville.getLibelleAcheminement(), ville.getLigne_5() } };

		// Creation String condition
		boolean conditionSQL = false;
		for (int i = 0; i < attributs[0].length; i++) {
			if (attributs[1][i] != null) {
				if (conditionSQL) {
					requeteWhere += "AND ";
				}
				requeteWhere += attributs[0][i] + "='" + attributs[1][i] + "' ";
				conditionSQL = true;
			}
		}

		// Assemblage des Strings requete
		String requeteFinale = requete;
		if (conditionSQL) {
			requeteFinale += requeteWhere;
		}
		return requeteFinale;
	}

}
