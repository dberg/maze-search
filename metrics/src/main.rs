extern crate ncurses;

use std::{env, fmt, fs};
use std::fmt::Formatter;
use ncurses::*;
use ncurses::CURSOR_VISIBILITY::CURSOR_INVISIBLE;

#[derive(Debug,PartialEq)]
struct Point { x: u32, y: u32 }

impl Point {
    fn is_equal(&self, x: u32, y: u32) -> bool { self.x == x && self.y == y }
}

impl fmt::Display for Point {
    fn fmt(&self, f: &mut Formatter<'_>) -> fmt::Result {
        write!(f, "({},{})", self.x, self.y)
    }
}

#[derive(Debug)]
struct Graph {
    rows: u32,
    cols: u32,
    ini: Point,
    end: Point,
    blocked: Vec<Point>,
    paths: Vec<Vec<Point>>,
}

impl Graph {
    fn empty() -> Graph {
        Graph {
            rows: 0, cols: 0,
            ini: Point { x: 0, y: 0 },
            end: Point { x: 0, y: 0 },
            blocked: vec!(),
            paths: vec!()
        }
    }

    fn is_valid(&self) -> bool {
        self.rows > 0 && self.cols > 0 && self.ini != self.end
    }

    fn blocked_to_string(&self) -> String {
        Self::points_to_string(&self.blocked)
    }

    fn paths_to_string(&self) -> String {
        self.paths.iter().map(|xs| format!("[{}]", Self::points_to_string(xs))).collect()
    }

    fn points_to_string(points: &Vec<Point>) -> String {
        points.iter().map(|x| x.to_string()).collect()
    }

}

impl fmt::Display for Graph {
    fn fmt(&self, f: &mut Formatter<'_>) -> fmt::Result {
        write!(
            f,
            "{{rows:{},cols:{},ini:{},end:{},blocked:{},paths:{}}}",
            self.rows,
            self.cols,
            self.ini,
            self.end,
            self.blocked_to_string(),
            self.paths_to_string()
        )
    }
}


fn main() {
    let graph = load_graph_data();
    render(&graph);
}

fn render(graph: &Graph) {
    initscr();
    noecho(); // don't echo key presses
    curs_set(CURSOR_INVISIBLE); // hide the cursor
    start_color(); // enable color functionality
    init_pair(1, COLOR_CYAN, COLOR_BLACK); // used for path
    init_pair(2, COLOR_GREEN, COLOR_BLACK); // used for start point
    init_pair(3, COLOR_WHITE, COLOR_YELLOW); // used for end point

    let mut idx = 0;
    while idx < graph.paths.len() {
        clear();
        render_frame(idx, graph);
        refresh(); // update the screen
        napms(50);
        idx += 1;
    }

    getch(); // wait for key press
    endwin();
}

fn render_frame(idx: usize, graph: &Graph) {
    // render grid with ini and end points
    for row in 0..graph.rows {
        for col in 0..graph.cols {
            let is_start = graph.ini.is_equal(row, col);
            let is_end = graph.end.is_equal(row, col);
            if is_start {
                attron(COLOR_PAIR(2));
                addch(A_ALTCHARSET() | ACS_CKBOARD());
                attroff(COLOR_PAIR(2));
            } else if is_end {
                attron(COLOR_PAIR(3));
                addch(A_ALTCHARSET() | ACS_STERLING());
                attroff(COLOR_PAIR(3));
            } else {
                addch(A_ALTCHARSET() | ACS_BULLET());
            }
        }
        addstr("\n");
    }

    // overwrite blocked cells
    for p in graph.blocked.iter() {
        mv(p.x as i32, p.y as i32);
        addch(A_ALTCHARSET() | ACS_DIAMOND());
    }

    // overwrite path
    let path = &graph.paths[idx];
    for p in path {
        let is_start = graph.ini.is_equal(p.x, p.y);
        let is_end = graph.end.is_equal(p.x, p.y);
        if is_start || is_end { continue; }
        mv(p.x as i32, p.y as i32);
        attron(COLOR_PAIR(1));
        addstr("*");
        attroff(COLOR_PAIR(1));
    }

    // legend
    mv((graph.rows as i32) + 1, 0);
    addstr("Legend\n");
    attron(COLOR_PAIR(2));
    addch(A_ALTCHARSET() | ACS_CKBOARD());
    attroff(COLOR_PAIR(2));
    addstr(" starting point \n");
    attron(COLOR_PAIR(3));
    addch(A_ALTCHARSET() | ACS_STERLING());
    attroff(COLOR_PAIR(3));
    addstr(" goal\n");
    addch(A_ALTCHARSET() | ACS_BULLET());
    addstr(" unexplored\n");
    attron(COLOR_PAIR(1));
    addstr("*");
    attroff(COLOR_PAIR(1));
    addstr(" path\n");
    addch(A_ALTCHARSET() | ACS_DIAMOND());
    addstr(" blocked");
}

fn load_graph_data() -> Graph {
    let args: Vec<String> = env::args().collect();
    let filename = args.get(1)
        .unwrap_or_else(|| panic!("Missing filename with graph data"));
    let contents = fs::read_to_string(filename)
        .unwrap_or_else(|error| panic!("Failed to read file {filename} with {error}"));
    let lines: Vec<&str> = contents.lines().collect();
    let mut graph = Graph::empty();
    for line in lines {
        parse_line(line, &mut graph)
    }
    if graph.is_valid() {
        graph
    } else {
        panic!("Invalid graph object {:?}", graph);
    }
}

fn parse_line(line: &str, graph: &mut Graph) {
    let parts: Vec<&str> = line.split(":").collect();
    if parts.len() == 0 {
        ()
    } else if parts.len() == 2 {
        let key = parts[0].trim();
        let val: Vec<char> = parts[1].trim().chars().collect();
        match key {
            "rows" => {
                let rows = parse_u32(&val);
                graph.rows = rows;
            },
            "cols" => {
                let cols = parse_u32(&val);
                graph.cols = cols;
            },
            "ini" => {
                let (idx, point) = consume_point(0, &val);
                assert_idx_at_end(idx, &val);
                graph.ini = point;
            },
            "end" => {
                let (idx, point) = consume_point(0, &val);
                assert_idx_at_end(idx, &val);
                graph.end = point;
            },
            "blocked" => {
                let (idx, points) = consume_points(0, &val);
                assert_idx_at_end(idx, &val);
                graph.blocked = points;
            },
            "paths" => {
                let (idx, path) = consume_path(&val);
                assert_idx_at_end(idx, &val);
                graph.paths = path;
            },
            _ => panic!("Invalid key {key} in line {line}")
        }
    } else {
        panic!("Invalid line format {line}");
    }
}

fn assert_idx_at_end(idx: usize, val: &Vec<char>) {
    if val.len() != idx {
        let s: String = val.into_iter().collect();
        panic!("Invalid characters found in {}", s);
    }
}

fn parse_u32(val: &Vec<char>) -> u32 {
    let s: String = val.into_iter().collect();
    s.parse::<u32>()
        .unwrap_or_else(|e| panic!("Failed to parse number {s} with error {e}"))
}

// Format
// (9,9)
fn consume_point(idx: usize, val: &Vec<char>) -> (usize, Point) {
    let mut idx = consume_char(idx, val, '(');
    let x;
    (idx, x) = consume_number(idx, val);
    idx = consume_char(idx, val, ',');
    let y;
    (idx, y) = consume_number(idx, val);
    idx = consume_char(idx, val, ')');
    (idx, Point { x, y })
}

// Format
// (9,9) (9,9) ...
fn consume_points(idx: usize, val: &Vec<char>) -> (usize, Vec<Point>) {
    let mut points = vec!();
    let mut idx: usize = idx;
    idx = consume_whitespace(idx, val);
    while idx < val.len() && val[idx] == '(' {
        let point;
        (idx, point) = consume_point(idx, val);
        points.push(point);
        idx = consume_whitespace(idx, val);
    }
    (idx, points)
}

// Format
// [(9,9)] [(9,9)(9,9)]...
fn consume_path(val: &Vec<char>) -> (usize, Vec<Vec<Point>>) {
    let mut path: Vec<Vec<Point>> = vec!();
    let mut idx: usize = 0;
    idx = consume_whitespace(idx, val);
    while idx < val.len() && val[idx] == '[' {
        idx = consume_char(idx, val, '[');
        let points;
        (idx, points) = consume_points(idx, val);
        path.push(points);
        idx = consume_char(idx, val, ']');
        idx = consume_whitespace(idx, val);
    }
    (idx, path)
}

fn consume_char(idx: usize, val: &Vec<char>, c: char) -> usize {
    let idx = consume_whitespace(idx, val);
    if val[idx] != c {
        let s: String = val.into_iter().collect();
        panic!("Expected char {c} but got {} in {}", val[idx], s);
    }
    idx + 1
}

fn consume_number(idx: usize, val: &Vec<char>) -> (usize, u32) {
    let mut idx = consume_whitespace(idx, val);
    let mut xs: Vec<char> = vec!();
    while idx < val.len() && val[idx].is_digit(10) {
        xs.push(val[idx]);
        idx += 1;
    }
    (idx, parse_u32(&xs))
}

fn consume_whitespace(idx: usize, val: &Vec<char>) -> usize {
    let mut idx = idx;
    while idx < val.len() && val[idx].is_whitespace() { idx += 1 }
    idx
}
