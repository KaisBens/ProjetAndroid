package com.example.sae41;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.List;
import java.util.ArrayList;

public class DrawView extends View {
    private Paint paint;
    private Puzzle puzzle;
    // Taille par défaut recalculée dans onSizeChanged
    private int cellSize = 150;
    private int radius = 30;

    private List<List<Point>> drawnPaths;
    private List<Point> currentPath;
    private boolean puzzleSolved = false;

    private boolean monochromeMode = false;
    private Paint[] paints;

    // Constructeur appelé lors de l'inflation depuis XML
    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
        drawnPaths = new ArrayList<>();
    }

    // Constructeur utilisé pour créer la vue avec un Puzzle fourni (non utilisé via XML)
    public DrawView(Context context, Puzzle puzzle) {
        super(context);
        this.puzzle = puzzle;
        init(context);
        drawnPaths = new ArrayList<>();
    }

    private void init(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        monochromeMode = prefs.getBoolean("monochrome", false);

        paint = new Paint();
        paint.setAntiAlias(true);

        if (puzzle != null) {
            List<Point[]> pairs = puzzle.getPairs();
            paints = new Paint[pairs.size()];
            for (int i = 0; i < paints.length; i++) {
                paints[i] = new Paint();
                paints[i].setAntiAlias(true);
                paints[i].setStrokeWidth(10);
                if (monochromeMode) {
                    int gray = 50 + (i * 180 / Math.max(1, paints.length));
                    paints[i].setColor(Color.rgb(gray, gray, gray));
                } else {
                    paints[i].setColor(Color.HSVToColor(new float[]{(i * 360f / paints.length), 1f, 1f}));
                }
            }
        }
    }

    public void setPuzzle(Puzzle puzzle) {
        this.puzzle = puzzle;
        drawnPaths = new ArrayList<>();
        List<Point[]> pairs = puzzle.getPairs();
        paints = new Paint[pairs.size()];
        for (int i = 0; i < paints.length; i++) {
            paints[i] = new Paint();
            paints[i].setAntiAlias(true);
            paints[i].setStrokeWidth(10);
            if (monochromeMode) {
                int gray = 50 + (i * 180 / Math.max(1, paints.length));
                paints[i].setColor(Color.rgb(gray, gray, gray));
            } else {
                paints[i].setColor(Color.HSVToColor(new float[]{(i * 360f / paints.length), 1f, 1f}));
            }
        }
        invalidate();
    }

    // Ajuste la taille des cellules pour que la grille ne dépasse pas les bords
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (puzzle != null) {
            cellSize = Math.min(w / puzzle.getSize(), h / puzzle.getSize());
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (puzzle == null) return;

        drawGrid(canvas);

        List<Point[]> pairs = puzzle.getPairs();
        for (int i = 0; i < pairs.size(); i++) {
            Point[] pair = pairs.get(i);
            Paint p = paints[i];
            for (Point pt : pair) {
                float x = pt.colonne * cellSize + cellSize / 2;
                float y = pt.ligne * cellSize + cellSize / 2;
                canvas.drawCircle(x, y, radius, p);
            }
        }

        for (int i = 0; i < drawnPaths.size(); i++) {
            drawPath(canvas, drawnPaths.get(i), paints[i % paints.length]);
        }

        if (currentPath != null) {
            Paint tempPaint = new Paint();
            tempPaint.setAntiAlias(true);
            tempPaint.setStrokeWidth(10);
            tempPaint.setColor(monochromeMode ? Color.DKGRAY : Color.RED);
            drawPath(canvas, currentPath, tempPaint);
        }
    }

    private void drawPath(Canvas canvas, List<Point> path, Paint paintToUse) {
        if (path.size() > 1) {
            for (int i = 0; i < path.size() - 1; i++) {
                Point p1 = path.get(i);
                Point p2 = path.get(i + 1);
                float x1 = p1.colonne * cellSize + cellSize / 2;
                float y1 = p1.ligne * cellSize + cellSize / 2;
                float x2 = p2.colonne * cellSize + cellSize / 2;
                float y2 = p2.ligne * cellSize + cellSize / 2;
                canvas.drawLine(x1, y1, x2, y2, paintToUse);
            }
        }
    }

    private void drawGrid(Canvas canvas) {
        Paint gridPaint = new Paint();
        gridPaint.setColor(Color.GRAY);
        gridPaint.setStrokeWidth(3);
        int size = puzzle.getSize();

        for (int i = 0; i <= size; i++) {
            canvas.drawLine(i * cellSize, 0, i * cellSize, size * cellSize, gridPaint);
            canvas.drawLine(0, i * cellSize, size * cellSize, i * cellSize, gridPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (puzzleSolved) return false;

        float eventX = event.getX();
        float eventY = event.getY();
        int col = (int) eventX / cellSize;
        int row = (int) eventY / cellSize;

        if (col < 0 || row < 0 || col >= puzzle.getSize() || row >= puzzle.getSize()) {
            cancelCurrentPath();
            return true;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Point snappedStart = getSnappedPoint(eventX, eventY);
                if (snappedStart != null) {
                    // Supprime les tracés existants dont le premier ou le dernier point correspond
                    for (int i = drawnPaths.size() - 1; i >= 0; i--) {
                        List<Point> path = drawnPaths.get(i);
                        if (!path.isEmpty() && (equals(path.get(0), snappedStart) || equals(path.get(path.size() - 1), snappedStart))) {
                            drawnPaths.remove(i);
                        }
                    }
                    currentPath = new ArrayList<>();
                    currentPath.add(snappedStart);
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (currentPath != null) {
                    Point lastPoint = currentPath.get(currentPath.size() - 1);
                    Point snappedMove = getSnappedPoint(eventX, eventY);
                    if (snappedMove == null) {
                        snappedMove = new Point(col, row);
                    }
                    if (equals(lastPoint, snappedMove)) {
                        // Rien à ajouter
                    } else if (isAdjacent(lastPoint, snappedMove) && !currentPathContains(snappedMove)) {
                        // Obtenir l'indice de la paire de la ligne en cours
                        int currentPairIndex = getPairIndexForPoint(currentPath.get(0));
                        // Si le nouveau point est un point puzzle
                        if (isPuzzlePoint(snappedMove)) {
                            int movePairIndex = getPairIndexForPoint(snappedMove);
                            // N'autoriser l'ajout que si c'est le point final valide de la même paire
                            if (movePairIndex == currentPairIndex && !equals(snappedMove, currentPath.get(0)) && isValidPair(currentPath.get(0), snappedMove)) {
                                currentPath.add(snappedMove);
                                // On arrête l'extension du tracé ici
                                return true;
                            } else {
                                // Si le point puzzle appartient à une autre couleur, ne pas l'ajouter
                                return true;
                            }
                        } else {
                            // Si ce n'est pas un point puzzle, vérifier qu'il n'est pas occupé par une ligne d'une autre couleur
                            if (isCellOccupiedByDifferentColor(snappedMove, currentPairIndex)) {
                                return true;
                            } else {
                                currentPath.add(snappedMove);
                            }
                        }
                    } else if (!equals(lastPoint, snappedMove) && currentPathContains(snappedMove)) {
                        cancelCurrentPath();
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
                if (currentPath != null && currentPath.size() > 0) {
                    Point snappedEnd = getSnappedPoint(eventX, eventY);
                    if (snappedEnd != null) {
                        currentPath.set(currentPath.size() - 1, snappedEnd);
                    }
                }
                if (isValidPath(currentPath)) {
                    drawnPaths.add(currentPath);
                    if (checkPuzzleSolved()) {
                        puzzleSolved = true;
                        Toast.makeText(getContext(), "Vous avez gagné", Toast.LENGTH_LONG).show();
                    }
                }
                currentPath = null;
                break;
        }
        invalidate();
        return true;
    }

    // Renvoie le point puzzle "snapé" si le toucher est proche d'un des centres, sinon null
    private Point getSnappedPoint(float x, float y) {
        float tolerance = 30; // tolérance en pixels
        for (Point[] pair : puzzle.getPairs()) {
            for (Point p : pair) {
                float centerX = p.colonne * cellSize + cellSize / 2;
                float centerY = p.ligne * cellSize + cellSize / 2;
                float dx = x - centerX;
                float dy = y - centerY;
                if (Math.sqrt(dx * dx + dy * dy) <= tolerance) {
                    return new Point(p.colonne, p.ligne);
                }
            }
        }
        return null;
    }

    // Vérifie si la case est déjà occupée par un tracé d'une couleur différente
    private boolean isCellOccupiedByDifferentColor(Point pt, int currentPairIndex) {
        for (List<Point> path : drawnPaths) {
            for (Point p : path) {
                if (equals(p, pt)) {
                    int pathPairIndex = getPairIndexForPoint(path.get(0));
                    if (pathPairIndex != currentPairIndex) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // Vérifie si un point correspond à un point puzzle défini (les cercles)
    private boolean isPuzzlePoint(Point pt) {
        for (Point[] pair : puzzle.getPairs()) {
            if (equals(pair[0], pt) || equals(pair[1], pt)) {
                return true;
            }
        }
        return false;
    }

    // Renvoie l'indice de la paire auquel appartient le point, ou -1 s'il n'est pas trouvé
    private int getPairIndexForPoint(Point pt) {
        List<Point[]> pairs = puzzle.getPairs();
        for (int i = 0; i < pairs.size(); i++) {
            Point[] pair = pairs.get(i);
            if (equals(pair[0], pt) || equals(pair[1], pt)) {
                return i;
            }
        }
        return -1;
    }

    // Vérifie si deux points forment une paire valide (la "bonne" couleur)
    private boolean isValidPair(Point a, Point b) {
        for (Point[] pair : puzzle.getPairs()) {
            if ((equals(pair[0], a) && equals(pair[1], b)) || (equals(pair[1], a) && equals(pair[0], b))) {
                return true;
            }
        }
        return false;
    }

    private boolean isAdjacent(Point p1, Point p2) {
        int dx = Math.abs(p1.colonne - p2.colonne);
        int dy = Math.abs(p1.ligne - p2.ligne);
        return (dx + dy == 1);
    }

    private boolean currentPathContains(Point pt) {
        for (Point p : currentPath) {
            if (p.colonne == pt.colonne && p.ligne == pt.ligne) {
                return true;
            }
        }
        return false;
    }

    private void cancelCurrentPath() {
        currentPath = null;
        invalidate();
    }

    private boolean isValidPath(List<Point> path) {
        if (path == null || path.size() < 2) return false;
        Point start = path.get(0);
        Point end = path.get(path.size() - 1);
        boolean validPair = false;
        for (Point[] pair : puzzle.getPairs()) {
            if ((equals(pair[0], start) && equals(pair[1], end)) ||
                    (equals(pair[1], start) && equals(pair[0], end))) {
                validPair = true;
                break;
            }
        }
        if (!validPair) return false;
        for (int i = 0; i < path.size() - 1; i++) {
            if (!isAdjacent(path.get(i), path.get(i + 1))) return false;
        }
        return true;
    }

    private boolean equals(Point a, Point b) {
        return a.colonne == b.colonne && a.ligne == b.ligne;
    }

    private boolean checkPuzzleSolved() {
        int size = puzzle.getSize();
        boolean[][] used = new boolean[size][size];
        for (List<Point> path : drawnPaths) {
            for (Point p : path) {
                if (used[p.ligne][p.colonne]) return false;
                used[p.ligne][p.colonne] = true;
            }
        }
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (!used[r][c]) return false;
            }
        }
        return true;
    }

    public ArrayList<int[]> savePaths() {
        ArrayList<int[]> result = new ArrayList<>();
        for (List<Point> path : drawnPaths) {
            for (Point p : path) {
                result.add(new int[]{p.colonne, p.ligne});
            }
            result.add(new int[]{-1, -1});
        }
        return result;
    }

    public void restorePaths(ArrayList<int[]> rawData) {
        drawnPaths.clear();
        List<Point> current = new ArrayList<>();
        for (int[] coord : rawData) {
            if (coord[0] == -1 && coord[1] == -1) {
                drawnPaths.add(current);
                current = new ArrayList<>();
            } else {
                current.add(new Point(coord[0], coord[1]));
            }
        }
        invalidate();
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }
}
