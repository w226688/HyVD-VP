# HyVD-VP Dataset with Preprocessing Scripts

In the paper **_“Fine-Tuning a Vulnerability-Specific Large Language Model for a Hybrid Software Vulnerability Detection Method”_**.

---

## 📘 Repository Description

This repository contains the HyVD-VP dataset used for model training and evaluation. It provides two types of code samples—**short code** for fine-tuning and **long code** for evaluation—as well as preprocessing scripts for generating standardized training data.

- **[`short_code/`](short_code)** — short code samples used for the two-phase fine-tuning procedure (SFT and DPO). The repository contains **18,580 short-code samples**, along with preprocessing outputs ([`sft_true.json`](short_code/sft_true.json), [`sft_false.json`](short_code/sft_false.json), [`dpo_true.json`](short_code/dpo_true.json), [`dpo_false.json`](short_code/dpo_false.json)).

- **[`long_code/`](long_code)** — multi-project long-code samples used to evaluate HyVD-VP’s vulnerability detection capability.Ground-truth vulnerability metadata is provided in [`long_code/expectedresults.csv`](long_code/expectedresults.csv). The directory currently includes **152 long-code samples**.

- **[`preprocessing/`](preprocessing)** — preprocessing scripts that keep transformation code separate from dataset files—including the [MegaVul](https://1drv.ms/f/s!AtzrzuojQf5sgeISZ9zN_4owVnUn9g) and [Juliet Java 1.3](https://samate.nist.gov/SARD/test-suites/102)—thereby improving auditability and reproducibility.
  - [`preprocessing/long_code_preprocessing.py`](preprocessing/long_code_preprocessing.py): preprocessing for long-code samples (conversion, cleaning, deduplication, CSV export).  
  - [`preprocessing/short_code_preprocessing.py`](preprocessing/short_code_preprocessing.py): preprocessing for short-code samples (MegaVul/Juliet extraction, normalization, deduplication, generation of `sft_*.json` and `dpo_*.json`).

This dataset is actively maintained and will continue to be updated.

---

## 📂 Repository Structure

```text
├── README.md
├── short_code
│   ├── sft_true.json
│   ├── sft_false.json
│   ├── dpo_true.json
│   └── dpo_false.json
├── long_code
│   ├── expectedresults.csv
│   ├── checkstyle-idea_java
│   ├── opentasks_java
│   ├── Plan_java
│   ├── wally_java
│   ├── carina_java
│   ├── openkoda_java
│   ├── JellyToggleButton_java
│   ├── Spanny_java
│   ├── beanshell_java
│   ├── nvidium_java
│   ├── InDoorSurfaceView_java
│   ├── SmartIM4IntelliJ_java
│   ├── android-titlebar_java
│   ├── neosemantics_java
│   ├── Google-Directions-Android_java
│   ├── SlimAdapter_java
│   ├── mysql-connector-j_java
│   ├── z_comic_new_java
│   ├── GSYRecordWave_java
│   ├── SSM_HRMS_java
│   ├── wx-api_java
│   ├── FocusResize_java
│   ├── RollViewPager_java
│   ├── android-justifiedtextview_java
│   ├── FlyoutMenus_java
│   ├── Renderers_java
│   ├── android-FlipView_java
│   ├── material-code-input_java
│   ├── RegexGenerator_java
│   ├── android-DecoView-charting_java
│   ├── loading-balls_java
│   ├── weixin-java-pay-demo_java
│   ├── RSyntaxTextArea_java
│   ├── ahoy-onboarding_java
│   ├── lin-cms-spring-boot_java
│   ├── DanmukuLight_java
│   ├── aho-corasick_java
│   ├── layering-cache_java
│   ├── typescript-generator_java
│   ├── CommonAdapter_java
│   ├── WorldGuard_java
│   ├── lavaplayer_java
│   ├── templatespider_java
│   ├── ColorArcProgressBar_java
│   ├── jprotobuf_java
│   ├── stateless4j_java
│   ├── CircleMenu_java
│   ├── PhotonCamera_java
│   ├── snapdrop-android_java
│   ├── BufferTextInputLayout_java
│   ├── Payara_java
│   ├── WeYueReader_java
│   ├── BubbleLayout_java
│   ├── Parallax-Layer-Layout_java
│   ├── Vorolay_java
│   ├── jpmml-evaluator_java
│   ├── No-Chat-Reports_java
│   ├── Volley-demo_java
│   ├── jcseg_java
│   ├── selendroid_java
│   ├── UrlImageViewHelper_java
│   ├── hbase-book_java
│   ├── ripme_java
│   ├── BottomBarLayout_java
│   ├── hasor_java
│   ├── remote-method-guesser_java
│   ├── Badge_java
│   ├── MixPush_java
│   ├── redis-replicator_java
│   ├── BD-JB-1250_java
│   ├── MaterialDesignExample_java
│   ├── UniversalVideoView_java
│   ├── AudioVideoRecordingSample_java
│   ├── MPermissions_java
│   ├── ToastCompat_java
│   ├── flash-netty_java
│   ├── react-native-restart_java
│   ├── download-navi_java
│   ├── TestFX_java
│   ├── Lucee_java
│   ├── ArrowDownloadButton_java
│   ├── pwm_java
│   ├── datacap_java
│   ├── TencentKona-8_java
│   ├── LocalVPN_java
│   ├── AnimRichEditor_java
│   ├── pdf-bookmark_java
│   ├── data-warehouse-learning_java
│   ├── TapClick_java
│   ├── ListBuddies_java
│   ├── AndroidExpandingViewLibrary_java
│   ├── owner_java
│   ├── Android-Easy-MultiDex_java
│   ├── JsonUnit_java
│   ├── TAB_java
│   ├── cron-utils_java
│   ├── hertzbeat_java
│   ├── screw_java
│   ├── jena_java
│   ├── langchain4j_java
│   ├── jfreechart_java
│   ├── grpc_java
│   ├── portfolio_java
│   ├── chatgpt_java
│   ├── android-demos_java
│   ├── EasyML_java
│   ├── spring-cloud-shop_java
│   ├── resteasy_java
│   ├── langchat_java
│   ├── qpython_java
│   ├── protege_java
│   ├── Doodle_java
│   ├── spring-testing_java
│   ├── jdonframework_java
│   ├── sonar-java_java
│   ├── Skript_java
│   ├── qupath_java
│   ├── javacc_java
│   ├── java-client_java
│   ├── RoboBinding_java
│   ├── jcodec_java
│   ├── android-slidingactivity_java
│   ├── SmartCamera_java
│   ├── jnr-ffi_java
│   ├── SmoothRefreshLayout_java
│   ├── glowroot_java
│   ├── milkman_java
│   ├── TinkersConstruct_java
│   ├── TornadoVM_java
│   ├── android-utils_java
│   ├── pine_java
│   ├── EmojiChat_java
│   ├── QuickReturn_java
│   ├── dslabs_java
│   ├── nlp-lang_java
│   ├── busybox_java
│   ├── OpenSubdiv_c
│   ├── PcapPlusPlus_c
│   ├── parallel-hashmap_c
│   ├── memgraph_c
│   ├── distcc_c
│   ├── raylib-go_c
│   ├── Nintendont_c
│   ├── KDU_c
│   ├── PaddleGAN_python
│   ├── FastSAM_python
│   ├── EfficientNet-PyTorch_python
│   ├── reactpy_python
│   ├── conditional-flow-matching_python
│   ├── PyInquirer_python
│   ├── PCV_python
│   ├── PanoHead_python
└── preprocessing
    ├── short_code_preprocessing.py
    └── long_code_preprocessing.py
```

---

## 🚀 Usage Examples

#### Short code preprocessing (generate SFT / DPO files)
Process MegaVul and/or Juliet and produce SFT/DPO artifacts:

```bash
# MegaVul only
python preprocessing/short_code_preprocessing.py --megavul path/to/megavul.json --output_dir path/to/short_code/

# Juliet only
python preprocessing/short_code_preprocessing.py --juliet path/to/juliet.json --output_dir path/to/short_code/

# Both
python preprocessing/short_code_preprocessing.py --megavul path/to/megavul.json --juliet path/to/juliet.json --output_dir path/to/short_code/
```

After running, [`short_code/`](short_code) will contain the generated `sft_*.json` and `dpo_*.json` files.

#### Long code preprocessing
Convert a project to text, clean and deduplicate, then export a CSV:

```bash
python preprocessing/long_code_preprocessing.py \
  --project_root path/to/long_code/<project_folder> \
  --output_txt_dir path/to/out_txt_dir \
  --output_csv path/to/long_code/expectedresults.csv
```

## Notes

Provide the correct input dataset paths before running the preprocessing scripts.

MegaVul and Juliet can be processed independently or together.

For long-code preprocessing, <project_folder> should point to the target project's source code directory.

Make sure the output directories are writable and have sufficient storage space

---

## 👥 Contributors

- ybq ([@ybq1](https://github.com/ybq1))
