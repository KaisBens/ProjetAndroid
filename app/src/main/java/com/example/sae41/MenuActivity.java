package com.example.sae41;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.AdapterView;
import android.view.View;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

public class MenuActivity extends Activity {
    private List<Puzzle> puzzleList;
    private ListView listView;
    public void ouvrirOptions(View v) {
        Intent intent = new Intent(this, ConfigActivity.class);
        startActivity(intent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        listView = (ListView) findViewById(R.id.listViewPuzzles);

        // Charger la liste des puzzles depuis les assets
        puzzleList = loadPuzzles();
        List<String> puzzleNames = new ArrayList<String>();
        for (Puzzle puzzle : puzzleList) {
            if (puzzle.isValid()) {
                puzzleNames.add(puzzle.getName());
            } else {
                puzzleNames.add(puzzle.getName() + " (invalide)");
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, puzzleNames);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new PuzzleItemClickListener());
    }

    private List<Puzzle> loadPuzzles() {
        List<Puzzle> puzzles = new ArrayList<Puzzle>();
        try {
            String[] puzzleFiles = getAssets().list("puzzles");
            if (puzzleFiles != null) {
                for (String fileName : puzzleFiles) {
                    Puzzle p = PuzzleParser.parsePuzzle(getAssets().open("puzzles/" + fileName), fileName);
                    puzzles.add(p);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return puzzles;
    }

    private class PuzzleItemClickListener implements AdapterView.OnItemClickListener {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Puzzle selectedPuzzle = puzzleList.get(position);
            if (selectedPuzzle.isValid()) {
                Intent intent = new Intent(MenuActivity.this, PuzzleActivity.class);
                intent.putExtra("puzzleFile", selectedPuzzle.getFileName());
                startActivity(intent);
            }
        }
    }

    @Override
    public void onBackPressed() {
        // Retour en arrière dans le menu => quitter l'application
        finish();
    }
}
