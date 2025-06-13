package com.pokemon.PokemonWord.Controller;

import java.util.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@Controller
@RequestMapping("/PokemonWord")
public class PokemonController {

    private static final String POKEAPI_URL = "https://pokeapi.co/api/v2/";
    private final RestTemplate restTemplate;

    public PokemonController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("darkMode", true);
        model.addAttribute("pageTitle", "Mundo Pokemon");

        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(POKEAPI_URL + "pokemon?limit=53", Map.class);
            List<Map<String, String>> pokemons = (List<Map<String, String>>) response.getBody().get("results");
            model.addAttribute("pokemons", pokemons != null ? pokemons : Collections.emptyList());
        } catch (Exception e) {
            model.addAttribute("error", "Error al consumir la API: " + e.getMessage());
        }
        return "Pokemones";
    }

    @GetMapping("/Details/{id}")
    public String getPokemonDetails(@PathVariable int id, Model model) {
        try {
            Map<String, Object> pokemonData = getApiData("pokemon/" + id);
            if (pokemonData == null) {
                model.addAttribute("error", "No se encontró el Pokémon");
                return "Details";
            }

            String name = (String) pokemonData.get("name");
            model.addAttribute("pokemon", pokemonData);
            model.addAttribute("cryUrl", "https://play.pokemonshowdown.com/audio/cries/" + name + ".mp3");

            // Obtener datos de especie y evolución
            processSpeciesData(pokemonData, model);

            // Obtener ubicaciones con detalles completos
            model.addAttribute("locationAreas", getPokemonLocations(id, name));

        } catch (Exception e) {
            model.addAttribute("error", "Error al obtener el Pokémon: " + e.getMessage());
        }
        return "Details";
    }

    private void processSpeciesData(Map<String, Object> pokemonData, Model model) {
        Map<String, Object> speciesData = getApiData(((Map<String, Object>) pokemonData.get("species")).get("url").toString());
        model.addAttribute("species", speciesData);

        if (speciesData != null && speciesData.containsKey("evolution_chain")) {
            Map<String, Object> evolutionData = getApiData(((Map<String, Object>) speciesData.get("evolution_chain")).get("url").toString());
            model.addAttribute("evolution", evolutionData);
        }
    }

    private List<Map<String, Object>> getPokemonLocations(int pokemonId, String pokemonName) {
        try {
            List<Map<String, Object>> encounters = getApiDataAsList("pokemon/" + pokemonId + "/encounters");
            List<Map<String, Object>> locationAreas = new ArrayList<>();

            for (Map<String, Object> encounter : encounters) {
                Map<String, Object> locationArea = (Map<String, Object>) encounter.get("location_area");
                if (locationArea != null) {
                    Map<String, Object> areaDetails = getApiData(locationArea.get("url").toString());
                    if (areaDetails != null) {
                        locationAreas.add(processLocationArea(areaDetails, pokemonName));
                    }
                }
            }
            return locationAreas;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private Map<String, Object> processLocationArea(Map<String, Object> areaDetails, String pokemonName) {
        Map<String, Object> locationData = new LinkedHashMap<>();
        locationData.put("name", areaDetails.get("name"));

        List<Map<String, Object>> filteredEncounters = new ArrayList<>();
        List<Map<String, Object>> pokemonEncounters = (List<Map<String, Object>>) areaDetails.get("pokemon_encounters");

        if (pokemonEncounters != null) {
            for (Map<String, Object> encounter : pokemonEncounters) {
                if (pokemonName.equals(((Map<String, Object>) encounter.get("pokemon")).get("name"))) {
                    filteredEncounters.add(encounter);
                }
            }
        }

        locationData.put("pokemon_encounters", filteredEncounters);
        return locationData;
    }

    private Map<String, Object> getApiData(String endpoint) {
        String url = endpoint.startsWith("http") ? endpoint : POKEAPI_URL + endpoint;
        return restTemplate.getForEntity(url, Map.class).getBody();
    }

    private List<Map<String, Object>> getApiDataAsList(String endpoint) {
        String url = endpoint.startsWith("http") ? endpoint : POKEAPI_URL + endpoint;
        return restTemplate.getForEntity(url, List.class).getBody();
    }
}
