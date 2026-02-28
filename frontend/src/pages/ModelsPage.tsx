import { FormEvent, useEffect, useState } from 'react';
import { activateModel, listModels, trainModel } from '../api/services';
import { ModelVersion } from '../types';

type TrainFormState = {
  datasetPolicy: string;
  epochs: string;
  batchSize: string;
  learningRate: string;
  rebalance: boolean;
};

export function ModelsPage() {
  const [models, setModels] = useState<ModelVersion[]>([]);
  const [trainForm, setTrainForm] = useState<TrainFormState>({
    datasetPolicy: 'AUTO_LABEL_ONLY',
    epochs: '100',
    batchSize: '128',
    learningRate: '0.001',
    rebalance: true,
  });
  const [loading, setLoading] = useState(true);
  const [training, setTraining] = useState(false);
  const [activatingId, setActivatingId] = useState<number | null>(null);
  const [statusMessage, setStatusMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const loadModels = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await listModels();
      setModels(response);
    } catch (loadError) {
      setError(loadError instanceof Error ? loadError.message : 'Failed to load model versions');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadModels();
  }, []);

  const onTrain = async (event: FormEvent) => {
    event.preventDefault();
    setTraining(true);
    setError(null);
    setStatusMessage(null);
    try {
      const response = await trainModel({
        datasetPolicy: trainForm.datasetPolicy,
        epochs: trainForm.epochs ? Number(trainForm.epochs) : undefined,
        batchSize: trainForm.batchSize ? Number(trainForm.batchSize) : undefined,
        learningRate: trainForm.learningRate ? Number(trainForm.learningRate) : undefined,
        rebalance: trainForm.rebalance,
      });
      setStatusMessage(`Training run ${response.runId} status: ${response.status}`);
      await loadModels();
    } catch (trainError) {
      setError(trainError instanceof Error ? trainError.message : 'Training request failed');
    } finally {
      setTraining(false);
    }
  };

  const onActivate = async (versionId: number) => {
    setActivatingId(versionId);
    setError(null);
    setStatusMessage(null);
    try {
      const response = await activateModel(versionId);
      setStatusMessage(`Model ${response.activeVersionId} is now active.`);
      await loadModels();
    } catch (activateError) {
      setError(activateError instanceof Error ? activateError.message : 'Activation failed');
    } finally {
      setActivatingId(null);
    }
  };

  return (
    <div className="page-grid">
      <section className="panel">
        <h2>Train Model</h2>
        <form onSubmit={onTrain} className="form-grid">
          <label className="field">
            <span>Dataset Policy</span>
            <input
              value={trainForm.datasetPolicy}
              onChange={(e) => setTrainForm((prev) => ({ ...prev, datasetPolicy: e.target.value }))}
              required
            />
          </label>
          <label className="field">
            <span>Epochs</span>
            <input
              type="number"
              min="1"
              value={trainForm.epochs}
              onChange={(e) => setTrainForm((prev) => ({ ...prev, epochs: e.target.value }))}
            />
          </label>
          <label className="field">
            <span>Batch Size</span>
            <input
              type="number"
              min="1"
              value={trainForm.batchSize}
              onChange={(e) => setTrainForm((prev) => ({ ...prev, batchSize: e.target.value }))}
            />
          </label>
          <label className="field">
            <span>Learning Rate</span>
            <input
              type="number"
              min="0.000001"
              step="0.0001"
              value={trainForm.learningRate}
              onChange={(e) => setTrainForm((prev) => ({ ...prev, learningRate: e.target.value }))}
            />
          </label>
          <label className="checkbox-field">
            <input
              type="checkbox"
              checked={trainForm.rebalance}
              onChange={(e) => setTrainForm((prev) => ({ ...prev, rebalance: e.target.checked }))}
            />
            <span>Rebalance classes</span>
          </label>
          <button className="btn btn-primary" type="submit" disabled={training}>
            {training ? 'Training...' : 'Start Training'}
          </button>
        </form>
      </section>

      <section className="panel">
        <h2>Model Versions</h2>
        {loading ? <p className="supporting">Loading models...</p> : null}
        {error ? <p className="error">{error}</p> : null}
        {statusMessage ? <p className="success">{statusMessage}</p> : null}
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Version</th>
                <th>Created At</th>
                <th>Status</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {models.length === 0 ? (
                <tr>
                  <td colSpan={5}>No model versions available yet.</td>
                </tr>
              ) : null}
              {models.map((model) => (
                <tr key={model.id}>
                  <td>{model.id}</td>
                  <td>{model.versionName}</td>
                  <td>{new Date(model.createdAt).toLocaleString()}</td>
                  <td>{model.active ? 'ACTIVE' : 'INACTIVE'}</td>
                  <td>
                    <button
                      type="button"
                      className="btn btn-ghost"
                      disabled={model.active || activatingId === model.id}
                      onClick={() => onActivate(model.id)}
                    >
                      {activatingId === model.id ? 'Activating...' : model.active ? 'Current' : 'Activate'}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </div>
  );
}
