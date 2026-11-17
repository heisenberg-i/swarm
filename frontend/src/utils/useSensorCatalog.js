// PURPOSE: Frontend helper used by SWARM pages (useSensorCatalog).
import { useEffect, useState } from 'react';
import { catalogAPI } from '../services/api';
import { SENSOR_LABELS, SENSOR_TYPES, SENSOR_UNITS } from './constants';

export default function useSensorCatalog() {
  const [types, setTypes] = useState(SENSOR_TYPES);
  const [labels, setLabels] = useState(SENSOR_LABELS);
  const [units, setUnits] = useState(SENSOR_UNITS);

  useEffect(() => {
    catalogAPI.getSensorTypes()
      .then(({ data }) => {
        const list = data.data || [];
        if (!list.length) return;
        setTypes(list.map((item) => item.code));
        const nextLabels = { ...SENSOR_LABELS };
        const nextUnits = { ...SENSOR_UNITS };
        list.forEach((item) => {
          if (item.code) {
            nextLabels[item.code] = item.label || item.code;
            nextUnits[item.code] = item.defaultUnit || nextUnits[item.code] || '';
          }
        });
        setLabels(nextLabels);
        setUnits(nextUnits);
      })
      .catch(() => {});
  }, []);

  return { types, labels, units };
}
