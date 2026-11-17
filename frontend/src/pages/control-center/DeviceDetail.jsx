// PURPOSE: Control Center page for device provisioning and hardware export.
import { useEffect, useState } from 'react';
import { useLocation, useNavigate, useParams } from 'react-router-dom';
import {
  Alert, Box, Button, Card, CardContent, Chip, CircularProgress, Dialog, DialogActions,
  DialogContent, DialogTitle, Grid, MenuItem, Stack, Table, TableBody, TableCell,
  TableHead, TableRow, TextField, Typography,
} from '@mui/material';
import { controlCenterAPI } from '../../services/api';
import CopyableId from '../../components/common/CopyableId';
import useSensorCatalog from '../../utils/useSensorCatalog';

function downloadJson(filename, payload) {
  const blob = new Blob([JSON.stringify(payload, null, 2)], { type: 'application/json' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = filename;
  link.click();
  URL.revokeObjectURL(url);
}

function exportableConfig(provisioning) {
  if (!provisioning) return {};
  const { deviceSecret, secretShownOnce, ...rest } = provisioning;
  return {
    ...rest,
    deviceSecret: deviceSecret || undefined,
    secretShownOnce,
  };
}

export default function DeviceDetail() {
  const { id } = useParams();
  const location = useLocation();
  const navigate = useNavigate();
  const { types, labels } = useSensorCatalog();
  const [device, setDevice] = useState(null);
  const [provisioning, setProvisioning] = useState(location.state?.provisioning || null);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(true);
  const [channelOpen, setChannelOpen] = useState(false);
  const [saving, setSaving] = useState(false);
  const [channelForm, setChannelForm] = useState({ channelId: '', measurementType: 'TEMPERATURE', instrumentTag: '' });

  const load = () => {
    setLoading(true);
    Promise.all([
      controlCenterAPI.getDevice(id),
      provisioning?.deviceSecret
        ? Promise.resolve({ data: { data: provisioning } })
        : controlCenterAPI.getProvisioning(id),
    ]).then(([deviceRes, provRes]) => {
      setDevice(deviceRes.data.data);
      setProvisioning((prev) => {
        const next = provRes.data.data;
        if (prev?.deviceSecret) {
          return { ...next, deviceSecret: prev.deviceSecret, secretShownOnce: true };
        }
        return next;
      });
      setError('');
    }).catch((e) => setError(e.response?.data?.message || 'Failed to load device'))
      .finally(() => setLoading(false));
  };

  useEffect(() => { load(); }, [id]);

  const rotate = async () => {
    if (!window.confirm('Rotate this device secret? The previous secret stops working immediately.')) return;
    setSaving(true);
    try {
      const { data } = await controlCenterAPI.rotateDeviceSecret(id);
      setProvisioning(data.data);
      setSuccess('New secret issued. Copy it now; it will not be shown again.');
      load();
    } catch (e) {
      setError(e.response?.data?.message || 'Failed to rotate secret');
    } finally {
      setSaving(false);
    }
  };

  const saveChannel = async () => {
    setSaving(true);
    try {
      await controlCenterAPI.upsertDeviceChannel(id, channelForm);
      setChannelOpen(false);
      setSuccess('Channel saved');
      load();
    } catch (e) {
      setError(e.response?.data?.message || 'Failed to save channel');
    } finally {
      setSaving(false);
    }
  };

  const copyConfig = async () => {
    await navigator.clipboard.writeText(JSON.stringify(exportableConfig(provisioning), null, 2));
    setSuccess('Hardware configuration copied');
  };

  if (loading && !device) return <CircularProgress />;
  if (!device) return <Alert severity="error">{error || 'Device not found'}</Alert>;

  return (
    <Box>
      <Button onClick={() => navigate('/devices')} sx={{ mb: 2 }}>Back to devices</Button>
      <Typography variant="h5" fontWeight={700}>{device.deviceName || device.deviceUid}</Typography>
      <Stack direction="row" spacing={1} sx={{ my: 1 }} flexWrap="wrap" useFlexGap>
        <Chip size="small" label={device.lifecycle} />
        <Chip size="small" label={device.connectionStatus || 'Unknown'} />
        <Chip size="small" label={device.deviceKind} />
      </Stack>
      {error && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError('')}>{error}</Alert>}
      {success && <Alert severity="success" sx={{ mb: 2 }} onClose={() => setSuccess('')}>{success}</Alert>}
      {provisioning?.deviceSecret && (
        <Alert severity="warning" sx={{ mb: 2 }}>
          One-time device secret: copy it now. SWARM stores only a hash after this page.
        </Alert>
      )}

      <Grid container spacing={2}>
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" sx={{ mb: 1 }}>Identity</Typography>
              <CopyableId label="Tenant ID" value={provisioning?.tenantId || device.tenantId} />
              <CopyableId label="Plant ID" value={provisioning?.plantId || device.plantId} />
              <CopyableId label="Device UID" value={device.deviceUid} />
              <CopyableId label="Device PK" value={device.id} />
              <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                {device.tenantName} · {device.plantName}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" sx={{ mb: 1 }}>MQTT</Typography>
              <CopyableId label="Broker" value={`${provisioning?.mqttBrokerHost}:${provisioning?.mqttBrokerPort}`} />
              <CopyableId label="Telemetry topic" value={provisioning?.mqttTelemetryTopic} />
              <CopyableId label="MQTT username" value={provisioning?.mqttAuthUsername} />
              {provisioning?.deviceSecret && <CopyableId label="MQTT password / device secret" value={provisioning.deviceSecret} />}
              <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                {provisioning?.mqttAuthHint}
              </Typography>
              <Stack direction="row" spacing={1} sx={{ mt: 2 }} flexWrap="wrap" useFlexGap>
                <Button variant="outlined" onClick={copyConfig}>Copy configuration</Button>
                <Button
                  variant="outlined"
                  onClick={() => downloadJson(`${device.deviceUid}-hardware.json`, exportableConfig(provisioning))}
                >
                  Download JSON
                </Button>
                <Button color="warning" onClick={rotate} disabled={saving}>Rotate secret</Button>
              </Stack>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                <Typography variant="h6">Sensor channels</Typography>
                <Button size="small" onClick={() => setChannelOpen(true)}>Add / edit channel</Button>
              </Box>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>Channel</TableCell>
                    <TableCell>Type</TableCell>
                    <TableCell>Unit</TableCell>
                    <TableCell>Tag</TableCell>
                    <TableCell>Node</TableCell>
                    <TableCell>Status</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {(device.channels || []).length === 0 && (
                    <TableRow><TableCell colSpan={6}>No channels yet.</TableCell></TableRow>
                  )}
                  {(device.channels || []).map((c) => (
                    <TableRow key={c.id || c.channelId}>
                      <TableCell>{c.channelId}</TableCell>
                      <TableCell>{labels[c.measurementType] || c.measurementType}</TableCell>
                      <TableCell>{c.engineeringUnit || '—'}</TableCell>
                      <TableCell>{c.instrumentTag || '—'}</TableCell>
                      <TableCell>{c.sensorNodeId || '—'}</TableCell>
                      <TableCell>{c.active ? (c.dataStatus || 'Active') : 'Disabled'}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12}>
          <Card>
            <CardContent>
              <Typography variant="h6" sx={{ mb: 1 }}>Expected payload ({provisioning?.schemaVersion})</Typography>
              <Box
                component="pre"
                sx={{
                  p: 2,
                  bgcolor: 'action.hover',
                  borderRadius: 1,
                  overflow: 'auto',
                  fontSize: 13,
                }}
              >
                {JSON.stringify(provisioning?.payloadExample || {}, null, 2)}
              </Box>
              <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                {(provisioning?.notes || []).join(' ')}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      <Dialog open={channelOpen} onClose={() => setChannelOpen(false)}>
        <DialogTitle>Sensor channel</DialogTitle>
        <DialogContent>
          <TextField
            fullWidth
            sx={{ mt: 1 }}
            label="Channel ID"
            value={channelForm.channelId}
            onChange={(e) => setChannelForm({ ...channelForm, channelId: e.target.value })}
          />
          <TextField
            select
            fullWidth
            sx={{ mt: 2 }}
            label="Measurement"
            value={channelForm.measurementType}
            onChange={(e) => setChannelForm({ ...channelForm, measurementType: e.target.value })}
          >
            {types.map((t) => <MenuItem key={t} value={t}>{labels[t] || t}</MenuItem>)}
          </TextField>
          <TextField
            fullWidth
            sx={{ mt: 2 }}
            label="Instrument tag"
            value={channelForm.instrumentTag}
            onChange={(e) => setChannelForm({ ...channelForm, instrumentTag: e.target.value })}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setChannelOpen(false)}>Cancel</Button>
          <Button variant="contained" disabled={!channelForm.channelId || saving} onClick={saveChannel}>Save</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
