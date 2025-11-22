"""
Project-level preprocessing script (Juliet + project text similarity filter).

- Extract target fields from Juliet Java 1.3 dataset (filtered by allowed CWEs).
- Convert a project's source tree to plain .txt files via CodebaseToText.
- Load previously-processed project texts (optional) and current project's texts,
  then compare each Juliet sample against that combined project-text corpus.
- If similarity >= 0.8 (single threshold) the sample is removed.
- Filter non-source files and output a CSV with the final records.
"""

import json
import os
import re
import argparse
from typing import List, Dict
from difflib import SequenceMatcher
from codebase_to_text import CodebaseToText


ALLOWED_CWE = {"CWE-79", "CWE-787", "CWE-89", "CWE-352",
               "CWE-22", "CWE-125", "CWE-78", "CWE-862"}

# Single similarity threshold to use for project-based filtering
SIMILARITY_THRESHOLD = 0.8


# ============================================================
# Utilities
# ============================================================
def load_json_dataset(path: str) -> List[Dict]:
    """Load a JSON file that contains a list of dicts."""
    with open(path, 'r', encoding='utf-8') as f:
        return json.load(f)


def normalize_code(code: str) -> str:
    """
    Normalize code text by removing comments and excessive whitespace.
    Returns an empty string for non-string inputs.
    """
    if not isinstance(code, str):
        return ""
    # remove single-line '//' comments
    code = re.sub(r"//.*", "", code)
    # remove block comments /* ... */
    code = re.sub(r"/\*.*?\*/", "", code, flags=re.DOTALL)
    # strip whitespace from each line and remove empty lines
    code = "\n".join(line.strip() for line in code.splitlines() if line.strip())
    return code


def similarity(a: str, b: str) -> float:
    """Token-level similarity using SequenceMatcher on token lists."""
    if not a or not b:
        return 0.0
    return SequenceMatcher(None, a.split(), b.split()).ratio()


def extract_vuln_lines_from_diff(diff_text: str) -> List[str]:
    """(If needed) Extract diff lines starting with '+' or '-'."""
    if not diff_text:
        return []
    lines = []
    for ln in diff_text.splitlines():
        if ln.startswith('+ ') or ln.startswith('- '):
            lines.append(ln)
    return lines


# ============================================================
# Project text conversion
# ============================================================
def convert_project_to_txt(project_root: str, output_dir: str):
    """
    Convert a project codebase into plain .txt files using CodebaseToText.
    The converter is expected to write multiple .txt files under output_dir.
    """
    converter = CodebaseToText(input_path=project_root, output_path=output_dir, output_type="txt")
    converter.get_file()


def load_txt_corpus(txt_dir: str) -> List[str]:
    """
    Load all .txt files under txt_dir, normalize them and return a list of normalized texts.
    Used for similarity comparisons.
    """
    corpus = []
    if not os.path.isdir(txt_dir):
        return corpus
    for root, _, files in os.walk(txt_dir):
        for fname in files:
            if not fname.lower().endswith(".txt"):
                continue
            fpath = os.path.join(root, fname)
            try:
                with open(fpath, 'r', encoding='utf-8') as f:
                    content = f.read()
                norm = normalize_code(content)
                if norm:
                    corpus.append(norm)
            except Exception:
                # ignore unreadable files
                continue
    return corpus


# ============================================================
# Juliet dataset extraction
# ============================================================
def preprocess_juliet_dataset(juliet_path: str) -> List[Dict]:
    """
    Extract target fields from Juliet JSON and filter by allowed CWE.
    Returns list of dicts containing:
      repo_name, real_vulnerability, file_path, func_name, vuln_code, line_number, cwe_id
    """
    raw = load_json_dataset(juliet_path)
    out = []
    for rec in raw:
        cwe = rec.get("cwe_id")
        if cwe not in ALLOWED_CWE:
            continue
        entry = {
            "repo_name": rec.get("repo_name", ""),
            "real_vulnerability": rec.get("real_vulnerability", False),
            "file_path": rec.get("file_path", ""),
            "func_name": rec.get("func_name", ""),
            "vuln_code": rec.get("vuln_code", ""),
            "line_number": rec.get("line_number", ""),
            "cwe_id": cwe
        }
        out.append(entry)
    return out


# ============================================================
# Filter / deduplicate by project corpus
# ============================================================
def filter_by_project_texts(samples: List[Dict], project_texts: List[str]) -> List[Dict]:
    """
    Remove sample if its normalized vuln_code is too similar to ANY text in project_texts.
    - Normalizes each sample's vuln_code.
    - Compares against each project text; if similarity >= threshold -> remove.
    Returns filtered list.
    """
    if not samples:
        return []
    if not project_texts:
        # If no project texts provided, return original samples unchanged
        return samples

    filtered = []
    # Pre-normalize project_texts for efficiency (ensure non-empty)
    proj_norms = [p for p in project_texts if p]

    for s in samples:
        code = s.get("vuln_code", "")
        code_norm = normalize_code(code)
        if not code_norm:
            # skip empty or invalid samples
            continue

        too_similar = False
        for pt in proj_norms:
            try:
                sim = similarity(code_norm, pt)
            except Exception:
                sim = 0.0
            if sim >= SIMILARITY_THRESHOLD:
                too_similar = True
                break

        if not too_similar:
            filtered.append(s)

    return filtered


# ============================================================
# File extension filter
# ============================================================
def filter_source_files(samples: List[Dict], allowed_exts=None) -> List[Dict]:
    """
    Remove samples whose file_path extension is not in allowed_exts.
    Default allowed_exts includes common source code extensions.
    """
    if allowed_exts is None:
        allowed_exts = {".java", ".py", ".c", ".cpp", ".js", ".kt"}
    out = []
    for s in samples:
        path = s.get("file_path", "") or ""
        ext = os.path.splitext(path)[1].lower()
        if ext in allowed_exts:
            out.append(s)
    return out


# ============================================================
# CSV output
# ============================================================
def write_csv(samples: List[Dict], csv_path: str):
    """
    Write final structured CSV with header:
    project, real_vulnerability, file, function, vuln_code, vuln_line_number, cwe_id
    """
    import csv
    fieldnames = ["project", "real_vulnerability", "file", "function", "vuln_code", "vuln_line_number", "cwe_id"]
    with open(csv_path, 'w', encoding='utf-8', newline='') as f:
        writer = csv.DictWriter(f, fieldnames=fieldnames)
        writer.writeheader()
        for s in samples:
            writer.writerow({
                "project": s.get("repo_name", ""),
                "real_vulnerability": s.get("real_vulnerability", False),
                "file": s.get("file_path", ""),
                "function": s.get("func_name", ""),
                "vuln_code": s.get("vuln_code", ""),
                "vuln_line_number": s.get("line_number", ""),
                "cwe_id": s.get("cwe_id", "")
            })


# ============================================================
# Main
# ============================================================
def main():
    parser = argparse.ArgumentParser(description="Project-level preprocessing (Juliet + project text similarity filter)")
    parser.add_argument("--juliet_path", type=str, required=True, help="Juliet JSON dataset path")
    parser.add_argument("--project_path", type=str, required=True, help="Path to project codebase to convert")
    parser.add_argument("--output_txt_dir", type=str, required=True, help="Directory to save project txt files")
    parser.add_argument("--output_csv", type=str, required=True, help="CSV output path")
    parser.add_argument("--previous_txt_dir", type=str, default=None,
                        help="Directory containing previously processed project txt files (optional)")
    args = parser.parse_args()

    # Extract Juliet records (filtered by allowed CWEs)
    samples = preprocess_juliet_dataset(args.juliet_path)

    # Convert current project to txt files (writes .txt into output_txt_dir)
    os.makedirs(args.output_txt_dir, exist_ok=True)
    convert_project_to_txt(args.project_path, args.output_txt_dir)

    # Load current project's texts
    current_texts = load_txt_corpus(args.output_txt_dir)

    # Load previous project texts and combine with current texts
    combined_texts = []
    if args.previous_txt_dir:
        combined_texts.extend(load_txt_corpus(args.previous_txt_dir))
    combined_texts.extend(current_texts)

    # Remove non-source-file Juliet entries
    samples = filter_source_files(samples)

    # Filter each sample by comparing to combined project texts using single threshold
    samples = filter_by_project_texts(samples, project_texts=combined_texts)

    # Write CSV
    write_csv(samples, args.output_csv)


if __name__ == "__main__":
    main()
