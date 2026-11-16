// PURPOSE: SWARM plant-app page: Sensors.
import { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import {
  Box, Typography, Button, Card, Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, Chip, IconButton, Dialog, DialogTitle, DialogContent,
  DialogActions, TextField, MenuItem, Grid, FormControl, InputLabel, Select,
  CircularProgress, Alert, Tabs, Tab,
} from '@mui/material';
import { Add, Edit, PowerSettingsNew, Delete, CellTower } from '@mui/icons-material';
import { sensorAPI, plantAPI, fieldDeviceAPI } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { SENSOR_LABELS, getSensorTypesForPlant } from '../utils/constants';
import { pageHeaderRow, pageTitleSx, responsiveSelect } from '../utils/responsive';
import ConnectDevice from './ConnectDevice';

const emptyForm = {
  plantId: '',
  nodeName: '',
  sensorType: 'TEMPERATURE',
  status: 'ACTIVE',
};

function connectionLabel(node) {
  if (!node.lastReadingAt) return 'No Data';
  const ageMs = Date.now() - new Date(node.lastReadingAt).getTime();
  if (Number.isNaN(ageMs)) return 'Unknown';
  if (ageMs > 10 * 60 * 1000) return 'Stale';
  return 'Live';
}

function instrumentSource(node) {
  return node.deviceChipId ? 'SWARM Hub' : 'Instrument';
}

function tabFromParams(searchParams) {
  const t = searchParams.get('tab');
  if (t === 'pair' || t === 'hubs') return t;
  return 'inventory';
}

export default function Sensors({ fixedPlantId, embedded = false, hidePlantSelector = false }) {
  const { isSuperAdmin, isOperator } = useAuth();
  const [searchParams, setSearchParams] = useSearchParams();
  const tab = tabFromParams(searchParams);

  const [nodes, setNodes] = useState([]);
  const [hubs, setHubs] = useState([]);
  const [plants, setPlants] = useState([]);
  const [loading, setLoading] = useState(true);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editId, setEditId] = useState(null);
  const [form, setForm] = useState({ ...emptyForm, plantId: fixedPlantId || '' });
  const [filterPlant, setFilterPlant] = useState(fixedPlantId || '');

  const setTab = (next) => {
    const params = new URLSearchParams(searchParams);
    if (next === 'pair' || next === 'hubs') params.set('tab', next);
    else params.delete('tab');
    setSearchParams(params, { replace: true });
  };

  const loadData = () => {
    const plantId = fixedPlantId || filterPlant || undefined;
    Promise.all([
      sensorAPI.getAll(),
      plantAPI.getAll(),
      fieldDeviceAPI.list(plantId || undefined).catch(() => ({ data: { data: [] } })),
    ])
      .then(([nodesRes, plantsRes, hubsRes]) => {
        setNodes(nodesRes.data.data || []);
        setHubs(hubsRes.data.data || []);
        const list = plantsRes.data.data || [];
        setPlants(fixedPlantId ? list.filter((p) => p.plantId === Number(fixedPlantId)) : list);
        if (fixedPlantId) setFilterPlant(Number(fixedPlantId));
      }).finally(() => setLoading(false));
  };

  useEffect(() => { loadData(); }, [fixedPlantId, filterPlant]);

  const filtered = filterPlant ? nodes.filter((n) => n.plantId === Number(filterPlant)) : nodes;
  const selectedPlant = plants.find((p) => p.plantId === Number(form.plantId));
  const sensorTypeOptions = getSensorTypesForPlant(selectedPlant);

  const handleSave = async () => {
    const payload = {
      plantId: Number(form.plantId),
      nodeName: form.nodeName,
      sensorType: form.sensorType,
      status: form.status || 'ACTIVE',
    };
    if (editId) await sensorAPI.update(editId, payload);
    else await sensorAPI.create(payload);
    setDialogOpen(false);
    loadData();
  };

  const handleToggle = async (id, currentStatus) => {
    await sensorAPI.toggle(id, currentStatus !== 'ACTIVE');
    loadData();
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Remove this instrument from the plant inventory?')) return;
    await sensorAPI.delete(id);
    loadData();
  };

  const openAddInstrument = () => {
    setEditId(null);
    setForm({ ...emptyForm, plantId: filterPlant || fixedPlantId || '' });
    setDialogOpen(true);
  };

  if (loading) return <CircularProgress />;

  return (
    <Box>
      <Box sx={pageHeaderRow}>
        <Box sx={{ minWidth: 0 }}>
          {!embedded && (
            <>
              <Typography variant="h5" sx={pageTitleSx}>Field Devices</Typography>
              <Typography variant="body2" color="text.secondary">
                Commission SWARM sensor hubs on site, then keep the plant’s instrument list
                (temperature, methane, pH, transmitters) in one inventory.
              </Typography>
            </>
          )}
        </Box>
        <Box sx={{ display: 'flex', gap: 2, flexDirection: { xs: 'column', sm: 'row' }, width: { xs: '100%', sm: 'auto' } }}>
          {!hidePlantSelector && tab === 'inventory' && (
            <FormControl size="small" sx={responsiveSelect}>
              <InputLabel>Plant</InputLabel>
              <Select value={filterPlant} label="Plant" onChange={(e) => setFilterPlant(e.target.value)}>
                <MenuItem value="">All plants</MenuItem>
                {plants.map((p) => <MenuItem key={p.plantId} value={p.plantId}>{p.plantName}</MenuItem>)}
              </Select>
            </FormControl>
          )}
          {tab === 'inventory' && (
            <Button variant="contained" startIcon={<CellTower />} onClick={() => setTab('pair')}>
              Pair Sensor Hub
            </Button>
          )}
          {tab === 'inventory' && isSuperAdmin && (
            <Button variant="outlined" startIcon={<Add />} onClick={openAddInstrument}>
              Add instrument
            </Button>
          )}
        </Box>
      </Box>

      <Tabs
        value={tab}
        onChange={(_e, value) => setTab(value)}
        sx={{ mb: 2, borderBottom: 1, borderColor: 'divider' }}
      >
        <Tab value="inventory" label="Inventory" />
        <Tab value="hubs" label="Hubs & channels" />
        <Tab value="pair" label="Pair Sensor Hub" />
      </Tabs>

      {tab === 'hubs' && (
        <Card>
          <TableContainer>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Device</TableCell>
                  <TableCell>Plant</TableCell>
                  <TableCell>UID</TableCell>
                  <TableCell>Chip</TableCell>
                  <TableCell>Connection</TableCell>
                  <TableCell>Firmware</TableCell>
                  <TableCell>Channels</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {hubs.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={7} align="center">
                      <Box sx={{ py: 3 }}>
                        <Typography fontWeight={600}>No registered hubs yet</Typography>
                        <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                          Pair a SWARM Sensor Hub to create a device identity, logical channels, and credentials.
                        </Typography>
                        <Button variant="contained" startIcon={<CellTower />} onClick={() => setTab('pair')}>
                          Pair Sensor Hub
                        </Button>
                      </Box>
                    </TableCell>
                  </TableRow>
                ) : hubs.map((h) => (
                  <TableRow key={h.id}>
                    <TableCell>{h.deviceName}</TableCell>
                    <TableCell>{h.plantName || h.plantId}</TableCell>
                    <TableCell>
                      <Typography variant="body2" fontFamily="monospace">{h.deviceUid}</Typography>
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2" fontFamily="monospace">{h.chipId || '—'}</Typography>
                    </TableCell>
                    <TableCell>
                      <Chip
                        size="small"
                        label={h.connectionStatus || 'Not Connected'}
                        color={h.connectionStatus === 'Connected' ? 'success' : 'default'}
                        variant="outlined"
                      />
                    </TableCell>
                    <TableCell>{h.firmwareVersion || 'Unknown'}</TableCell>
                    <TableCell>
                      {(h.channels || []).map((c) => (
                        <Chip
                          key={c.channelId}
                          size="small"
                          sx={{ mr: 0.5, mb: 0.5 }}
                          label={`ch${c.channelId}: ${c.instrumentTag || c.measurementType} (${c.dataStatus || 'No Data'})`}
                        />
                      ))}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </Card>
      )}

      {tab === 'pair' && (
        <ConnectDevice
          embedded
          fixedPlantId={fixedPlantId || filterPlant || undefined}
          hidePlantSelector={hidePlantSelector || Boolean(fixedPlantId || filterPlant)}
          onPaired={() => {
            loadData();
            setTab('inventory');
          }}
        />
      )}

      {tab === 'inventory' && (
        <>
          {isOperator && (
            <Alert severity="info" sx={{ mb: 2 }}>
              You can pair a SWARM hub on this plant’s Wi‑Fi. Super Admin maintains the instrument list.
            </Alert>
          )}
          {!isSuperAdmin && !isOperator && (
            <Alert severity="info" sx={{ mb: 2 }}>
              Pair hubs here. Contact Super Admin to add P&amp;ID instruments that are not on a SWARM hub.
            </Alert>
          )}

          <Card>
            <TableContainer>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Tag / name</TableCell>
                    <TableCell>Plant</TableCell>
                    <TableCell>Measurement</TableCell>
                    <TableCell>Source</TableCell>
                    <TableCell>Hub IP</TableCell>
                    <TableCell>Chip ID</TableCell>
                    <TableCell>Last value</TableCell>
                    <TableCell>Data</TableCell>
                    <TableCell>Status</TableCell>
                    {isSuperAdmin && <TableCell>Actions</TableCell>}
                  </TableRow>
                </TableHead>
                <TableBody>
                  {filtered.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={isSuperAdmin ? 10 : 9} align="center">
                        <Box sx={{ py: 4, maxWidth: 520, mx: 'auto' }}>
                          <Typography fontWeight={600} sx={{ mb: 1 }}>No field devices on this plant yet</Typography>
                          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                            On site, join the same Wi‑Fi as the SWARM MODEL hub, then pair it.
                            That creates temperature, humidity, and methane points automatically.
                            Use Add instrument only for standalone P&amp;ID transmitters (pH, FIT, PIT, TIT).
                          </Typography>
                          <Button variant="contained" startIcon={<CellTower />} onClick={() => setTab('pair')}>
                            Pair Sensor Hub
                          </Button>
                        </Box>
                      </TableCell>
                    </TableRow>
                  ) : filtered.map((n) => (
                    <TableRow key={n.nodeId}>
                      <TableCell>{n.nodeName}</TableCell>
                      <TableCell>{n.plantName}</TableCell>
                      <TableCell>{SENSOR_LABELS[n.sensorType] || n.sensorType}</TableCell>
                      <TableCell>
                        <Chip
                          size="small"
                          label={instrumentSource(n)}
                          color={n.deviceChipId ? 'primary' : 'default'}
                          variant={n.deviceChipId ? 'filled' : 'outlined'}
                        />
                      </TableCell>
                      <TableCell>
                        {n.deviceIp ? (
                          <Typography variant="body2" fontFamily="monospace">{n.deviceIp}</Typography>
                        ) : '--'}
                      </TableCell>
                      <TableCell>
                        {n.deviceChipId ? (
                          <Typography variant="body2" fontFamily="monospace">{n.deviceChipId}</Typography>
                        ) : '--'}
                      </TableCell>
                      <TableCell>{n.lastValue != null ? n.lastValue.toFixed(2) : 'No Data'}</TableCell>
                      <TableCell>
                        <Chip
                          label={connectionLabel(n)}
                          size="small"
                          color={connectionLabel(n) === 'Live' ? 'success' : connectionLabel(n) === 'Stale' ? 'warning' : 'default'}
                          variant="outlined"
                        />
                      </TableCell>
                      <TableCell>
                        <Chip
                          label={n.status || 'Unknown'}
                          size="small"
                          color={n.status === 'ACTIVE' ? 'success' : n.status === 'OFFLINE' ? 'error' : 'default'}
                        />
                      </TableCell>
                      {isSuperAdmin && (
                        <TableCell>
                          <IconButton size="small" onClick={() => handleToggle(n.nodeId, n.status)} title="Enable or disable">
                            <PowerSettingsNew />
                          </IconButton>
                          <IconButton
                            size="small"
                            onClick={() => {
                              setEditId(n.nodeId);
                              setForm({ ...n, plantId: n.plantId });
                              setDialogOpen(true);
                            }}
                          >
                            <Edit />
                          </IconButton>
                          <IconButton size="small" color="error" onClick={() => handleDelete(n.nodeId)}>
                            <Delete />
                          </IconButton>
                        </TableCell>
                      )}
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          </Card>
        </>
      )}

      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>{editId ? 'Edit instrument' : 'Add instrument'}</DialogTitle>
        <DialogContent>
          <Typography variant="body2" color="text.secondary" sx={{ mt: 1, mb: 1 }}>
            Use this for P&amp;ID instruments that are not on a SWARM hub. Hub points
            (temperature, humidity, methane) are created when you pair the hub.
          </Typography>
          <Grid container spacing={2} sx={{ mt: 0 }}>
            <Grid item xs={12}>
              <TextField fullWidth select label="Plant" value={form.plantId} onChange={(e) => setForm({ ...form, plantId: e.target.value })}>
                {plants.map((p) => <MenuItem key={p.plantId} value={p.plantId}>{p.plantName}</MenuItem>)}
              </TextField>
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Tag / name"
                value={form.nodeName}
                onChange={(e) => setForm({ ...form, nodeName: e.target.value })}
                placeholder="e.g. TIT-103 Digester temperature"
              />
            </Grid>
            <Grid item xs={12}>
              <TextField fullWidth select label="Measurement" value={form.sensorType} onChange={(e) => setForm({ ...form, sensorType: e.target.value })}>
                {sensorTypeOptions.map((t) => <MenuItem key={t} value={t}>{SENSOR_LABELS[t] || t}</MenuItem>)}
              </TextField>
            </Grid>
            <Grid item xs={12}>
              <Typography variant="caption" color="text.secondary">
                Firmware, battery, and signal appear only when a hub reports them. They are never invented here.
              </Typography>
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={handleSave} disabled={!form.plantId || !form.nodeName}>Save</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
