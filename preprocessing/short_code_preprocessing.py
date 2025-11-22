"""
Unified preprocessing pipeline for MegaVul + Juliet Java 1.3 dataset.
MegaVul dataset can be downloaded from https://1drv.ms/f/s!AtzrzuojQf5sgeISZ9zN_4owVnUn9g
Juliet Java 1.3 dataset can be downloaded from https://samate.nist.gov/SARD/test-suites/102
Extract fields, normalize code, deduplicate, and output SFT-ready format.
"""

import json
import os
import re
import random
import copy
import argparse
from typing import List, Dict
from difflib import SequenceMatcher

ALLOWED_CWE = {"CWE-79", "CWE-787", "CWE-89", "CWE-352",
               "CWE-22", "CWE-125", "CWE-78", "CWE-862"}

# Single similarity threshold to use for filtering
SIMILARITY_THRESHOLD = 0.9

# ============================================================
# Unified loader
# ============================================================

def load_json_dataset(path: str) -> List[Dict]:
    """
    Unified JSON loader for both MegaVul and Juliet datasets.
    They share the same JSON structure (list of dicts), so one loader is enough.
    """
    with open(path, 'r', encoding='utf-8') as f:
        return json.load(f)


# ============================================================
# Common utilities
# ============================================================

def normalize_code(code: str) -> str:
    """
    Normalize code: strip comments and excessive whitespace.
    Used for both MegaVul and Juliet.
    """
    code = re.sub(r"//.*", "", code)
    code = re.sub(r"/\*.*?\*/", "", code, flags=re.DOTALL)
    code = "\n".join(line.strip() for line in code.splitlines() if line.strip())
    return code


def similarity(a: str, b: str) -> float:
    """Token-level similarity."""
    return SequenceMatcher(None, a.split(), b.split()).ratio()


def extract_vuln_lines(diff_text: str) -> List[str]:
    """MegaVul-specific: diff lines begin with + or -."""
    vuln = []
    for line in diff_text.splitlines():
        if line.startswith('+ ') or line.startswith('- '):
            vuln.append(line)
    return vuln


# ============================================================
# MegaVul Processing
# ============================================================

def extract_megavul_fields(sample: Dict) -> Dict:
    """Extract MegaVul required fields."""
    return {
        "cwe_ids": sample.get("cwe_ids", []),
        "diff_func": sample.get("diff_func", ""),
        "is_vul": sample.get("is_vul", "False")
    }


def preprocess_megavul(path: str) -> List[Dict]:
    """
    Full preprocessing for MegaVul dataset.
    Steps:
      1) load raw json
      2) filter allowed CWE
      3) normalize code
      4) similarity dedup
      5) extract vuln lines from diff
    """
    raw = load_json_dataset(path)

    data = []
    for s in raw:
        if any(c in ALLOWED_CWE for c in s.get("cwe_ids", [])):
            item = extract_megavul_fields(s)
            item["diff_func_norm"] = normalize_code(item["diff_func"])
            item["func"] = s.get("func", s.get("diff_func", ""))
            data.append(item)

    # Deduplicate based on normalized diff
    unique = []
    for s in data:
        is_dup = False
        for u in unique:
            if similarity(s["diff_func_norm"], u["diff_func_norm"]) >= SIMILARITY_THRESHOLD:
                is_dup = True
                break
        if not is_dup:
            unique.append(s)

    # Build final structured dict
    output = []
    for s in unique:
        vuln_lines = extract_vuln_lines(s["diff_func"])
        output.append({
            "is_vul": s["is_vul"],
            "cwe_id": s["cwe_ids"],
            "vuln_line": vuln_lines,
            "func": s["func"]
        })

    return output


# ============================================================
# Juliet Processing
# ============================================================

def preprocess_juliet(path: str) -> List[Dict]:
    """
    Preprocess Juliet Java 1.3 dataset.
    Steps:
      1) load json
      2) filter allowed CWE IDs
      3) extract simple fields
      4) normalize code
      5) deduplicate (90% similarity)
      6) output final structured dict
    """
    raw = load_json_dataset(path)

    data = []
    for s in raw:
        cwe = s.get("cwe_id")

        # Juliet CWE filtering (same as MegaVul)
        if cwe not in ALLOWED_CWE:
            continue

        entry = {
            "cwe_id": cwe,
            "vuln_code": s.get("vuln_code", ""),
            "real_vulnerability": s.get("real_vulnerability", False),
            "func": s.get("func", s.get("vuln_code", ""))
        }

        # Normalize Juliet code
        entry["vuln_code_norm"] = normalize_code(entry["vuln_code"])
        data.append(entry)

    # Deduplication (token similarity >= 0.9 means duplicate) 
    unique = []
    for s in data:
        is_dup = False
        for u in unique:
            if similarity(s["vuln_code_norm"], u["vuln_code_norm"]) >= SIMILARITY_THRESHOLD:
                is_dup = True
                break
        if not is_dup:
            unique.append(s)

    # Final formatted output
    output = []
    for s in unique:
        output.append({
            "is_vul": s["real_vulnerability"],
            "cwe_id": s["cwe_id"],
            "vuln_line": s["vuln_code"].splitlines(),
            "func": s["func"]
        })

    return output


# ============================================================
# Build SFT Format
# ============================================================

def build_sft_item(inp: str, out_obj: Dict) -> Dict:
    instruction = (
        "Analyze the following code to identify any CWE vulnerabilities. Provide the result in a JSON format. "
        "If vulnerabilities are found, list the vulnerable line(s) and their CWE ID(s), as shown in this example: "
        "{\"is_vul\": \"True\", \"cwe_id\": [\"CWE-89\"], \"vuln_line\": [\" return '\" + param + \"'\" ]}. "
        "If the code is free of vulnerabilities, leave the cwe_id and vuln_line fields empty. "
        "Do not include any explanations, just the JSON output."
    )
    return {
        "instruction": instruction,
        "input": inp,
        "output": json.dumps(out_obj, ensure_ascii=False)
    }


def convert_all_to_sft(samples: List[Dict]) -> (List[Dict], List[Dict]):
    """Split into SFT positive and negative sets."""
    sft_true = []
    sft_false = []

    for s in samples:
        code = s.get("func", "\n".join(s["vuln_line"]))
        sft_item = build_sft_item(code, s)

        if s["is_vul"] in ["True", True]:
            sft_true.append(sft_item)
        else:
            sft_false.append(sft_item)

    return sft_true, sft_false


def postprocess_dpo(sft_true_path, sft_false_path, output_dir):
    """
    Convert sft_true.json and sft_false.json to DPO format with 'chosen' and 'rejected'.
    Writes dpo_true.json and dpo_false.json
    """

    # Process sft_true.json
    with open(sft_true_path, 'r', encoding='utf-8') as f:
        sft_true = json.load(f)

    dpo_true = []
    for item in sft_true:
        new_item = copy.deepcopy(item)
        new_item["input"] = item.get("input", item.get("func", ""))
        new_item["chosen"] = new_item.pop("output")
        new_item["rejected"] = json.dumps({"is_vul": "False", "cwe_id": [], "vuln_line": []}, ensure_ascii=False)
        dpo_true.append(new_item)

    with open(os.path.join(output_dir, "dpo_true.json"), "w", encoding='utf-8') as f:
        json.dump(dpo_true, f, indent=2, ensure_ascii=False)

    # Process sft_false.json
    with open(sft_false_path, 'r', encoding='utf-8') as f:
        sft_false = json.load(f)

    dpo_false = []
    for item in sft_false:
        new_item = copy.deepcopy(item)
        new_item["input"] = item.get("input", item.get("func", ""))
        new_item["chosen"] = new_item.pop("output")

        try:
            lines = new_item["input"].splitlines()
            if len(lines) >= 2:
                vuln_lines = random.sample(lines, min(2, len(lines)))
            else:
                vuln_lines = lines
        except Exception:
            vuln_lines = None

        rejected_obj = {
            "is_vul": "True",
            "cwe_id": [random.choice(list(ALLOWED_CWE))],
            "vuln_line": vuln_lines
        }
        new_item["rejected"] = json.dumps(rejected_obj, ensure_ascii=False)

        dpo_false.append(new_item)

    with open(os.path.join(output_dir, "dpo_false.json"), "w", encoding='utf-8') as f:
        json.dump(dpo_false, f, indent=2, ensure_ascii=False)


# ============================================================
# Main
# ============================================================
def main():
    parser = argparse.ArgumentParser(description="MegaVul + Juliet Preprocessing Pipeline")
    parser.add_argument("--megavul", type=str, help="Path to MegaVul JSON dataset", default=None)
    parser.add_argument("--juliet", type=str, help="Path to Juliet JSON dataset", default=None)
    parser.add_argument("--output_dir", type=str, required=True, help="Directory to save SFT files")

    args = parser.parse_args()

    if args.megavul is None and args.juliet is None:
        raise ValueError("You must provide at least one dataset using --megavul and/or --juliet")

    os.makedirs(args.output_dir, exist_ok=True)

    merged = []

    # Process MegaVul if provided
    if args.megavul is not None:
        megavul_processed = preprocess_megavul(args.megavul)
        merged.extend(megavul_processed)

    # Process Juliet if provided
    if args.juliet is not None:
        juliet_processed = preprocess_juliet(args.juliet)
        merged.extend(juliet_processed)

    # Convert to SFT format
    sft_true, sft_false = convert_all_to_sft(merged)

    # Save output
    with open(f"{args.output_dir}/sft_true.json", "w", encoding="utf-8") as f:
        json.dump(sft_true, f, indent=2, ensure_ascii=False)

    with open(f"{args.output_dir}/sft_false.json", "w", encoding="utf-8") as f:
        json.dump(sft_false, f, indent=2, ensure_ascii=False)

    # Generate DPO files
    postprocess_dpo(
        sft_true_path=f"{args.output_dir}/sft_true.json",
        sft_false_path=f"{args.output_dir}/sft_false.json",
        output_dir=args.output_dir
    )

if __name__ == "__main__":
    main()
