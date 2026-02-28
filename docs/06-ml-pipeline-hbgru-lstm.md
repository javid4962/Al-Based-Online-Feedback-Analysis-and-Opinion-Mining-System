# 06 - ML Pipeline (HBGRU-LSTM)

## Objective

Binary sentiment classification with higher robustness for negative sentiment detection.

## Preprocessing

1. Lowercase conversion.
2. URL/HTML/special character cleanup.
3. Stopword removal with preserved negation/boosters:
- keep: `not`, `never`, `no`, `very`, `extremely`.
4. Tokenization.
5. Sequence padding/truncation to fixed max length.

## Model Shape

- Embedding layer: 128 dims.
- Bidirectional GRU: 256 units.
- LSTM: 128 units.//LSTM means Long-Short Term Memory
- Dense output: 1 neuron + sigmoid.
- Dropout: 0.20.

## Training Configuration

- Optimizer: Adam (`lr=0.001`).
- Batch size: 128.
- Epochs: up to 100.
- Early stopping patience: 8.
- Loss: binary cross-entropy.

## Evaluation Rules

- Report: accuracy, precision, recall, F1, confusion matrix.
- Enforce gate: negative-class F1 must be `>= 0.93`.
- Block model activation when candidate underperforms active model on critical class metrics.

## Artifact Rules

- Each training run writes:
- model binary
- preprocessing vocabulary/config snapshot
- metrics JSON
- confusion matrix JSON
