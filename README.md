# Maze Search

This project was inspired by the book [Classic Computer Science Problems in Java](https://www.amazon.com/Classic-Computer-Science-Problems-Java/dp/1617297607/ref=sr_1_1). The algorithms dfs, bfs, and astar are implemented in [kotlin](search).

There's an option to instrument the code and generate the animation of the
search via a small [rust program](metrics) and ncurses.

## dfs
![dfs](metrics/assets/dfs.gif)

## bfs
![bfs](metrics/assets/bfs.gif)

## astar
![astar](metrics/assets/astar.gif)

```bash
cd search
./gradlew run
```

If you want to instrument the code and generate logs that can be parsed to
generated the animated gifs run

```bash
cd search
./gradlew run --args='-i'
```

The following files should have been created

```
/tmp/graph-instrumented-astar.log
/tmp/graph-instrumented-bfs.log
/tmp/graph-instrumented-dfs.log
```

# Metrics

Animate the search logs

```bash
cd metrics
cargo run -- assets/graph-instrumented-astar.log
cargo run -- assets/graph-instrumented-dfs.log
cargo run -- assets/graph-instrumented-bfs.log

# or use the ones you've generated
cargo run -- /tmp/graph-instrumented-astar.log
cargo run -- /tmp/graph-instrumented-dfs.log
cargo run -- /tmp/graph-instrumented-bfs.log
```

# Generating animated gif assets

Install dependencies

```bash
brew install asciinema
cargo install --git https://github.com/asciinema/agg
```

Generate animated gifs

```
cd metrics
# same pattern for bfs and dfs
asciinema rec assets/astar.cast
# run ncurses animation
cargo run -- assets/graph-instrumented-astar.log
# ctrl-c to end, then ctrd-d to end asciinema recording
agg assets/astar.cast assets/astar.gif
```
