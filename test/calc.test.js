import { describe, it, expect, beforeEach } from 'vitest';
import { loadCalc } from './helpers/loadCalc.js';

// `sandbox` expone las mismas funciones/constantes globales que calc.js y
// config.js definen en el navegador (acumulado, projection, PASS_GRADE, ...).
// Se recarga antes de cada test para evitar que un test contamine a otro.
let sandbox;
beforeEach(() => {
  sandbox = loadCalc();
});

const DEFAULT_WEIGHTS = [0.15, 0.15, 0.15, 0.15, 0.20, 0.20];

function makeCourse(overrides = {}) {
  return {
    grades: [null, null, null, null, null, null],
    weights: DEFAULT_WEIGHTS.slice(),
    sustitutorio: null,
    goal: 11,
    creditos: 4,
    careerId: 'career-1',
    ...overrides,
  };
}

describe('effectiveGrades / enteredIndices / missingIndices', () => {
  it('devuelve las notas tal cual cuando no hay sustitutorio', () => {
    const c = makeCourse({ grades: [15, 10, null, null, null, null] });
    expect(sandbox.effectiveGrades(c)).toEqual([15, 10, null, null, null, null]);
    expect(sandbox.enteredIndices(c)).toEqual([0, 1]);
    expect(sandbox.missingIndices(c)).toEqual([2, 3, 4, 5]);
  });

  it('reemplaza el índice objetivo cuando hay sustitutorio aplicado', () => {
    const c = makeCourse({
      grades: [15, 8, null, 12, null, null],
      sustitutorio: { value: 18, targetIndex: 1, originalValue: 8 },
    });
    expect(sandbox.effectiveGrades(c)[1]).toBe(18);
    expect(sandbox.enteredIndices(c)).toEqual([0, 1, 3]);
  });
});

describe('acumulado / remainingWeight', () => {
  it('calcula la suma ponderada solo de lo ingresado', () => {
    const c = makeCourse({ grades: [15, 10, null, null, null, null] });
    // 15*0.15 + 10*0.15 = 2.25 + 1.5 = 3.75
    expect(sandbox.acumulado(c)).toBeCloseTo(3.75, 6);
    // peso restante: 0.15+0.15+0.20+0.20
    expect(sandbox.remainingWeight(c)).toBeCloseTo(0.70, 6);
  });

  it('con todo ingresado, acumulado = promedio ponderado total y no queda peso restante', () => {
    const c = makeCourse({ grades: [12, 12, 12, 12, 12, 12] });
    expect(sandbox.acumulado(c)).toBeCloseTo(12, 6);
    expect(sandbox.remainingWeight(c)).toBeCloseTo(0, 6);
  });
});

describe('projection', () => {
  it('devuelve null si no hay ninguna nota ingresada', () => {
    const c = makeCourse();
    expect(sandbox.projection(c)).toBeNull();
  });

  it('con una sola nota ingresada, proyecta esa misma nota en todo lo demás (línea plana)', () => {
    const c = makeCourse({ grades: [16, null, null, null, null, null] });
    // Si se proyecta 16 en las 6 evaluaciones, el acumulado ponderado total es 16
    // (los pesos suman 100%).
    expect(sandbox.projection(c)).toBeCloseTo(16, 6);
  });

  it('con dos notas de la MISMA unidad (C1 y P1), proyecta su promedio en línea plana', () => {
    // Caso real: 19 en continua y 17 en parcial de la unidad 1. No hay tendencia
    // entre unidades todavía, así que lo que falta se proyecta en el promedio (18).
    const c = makeCourse({ grades: [19, 17, null, null, null, null] });
    expect(sandbox.projection(c)).toBeCloseTo(18, 6);
  });

  it('con notas de unidades distintas, ajusta una recta por unidad y clipea entre 0 y 20', () => {
    const c = makeCourse({ grades: [10, 10, 14, null, null, null] });
    // puntos (unidad, nota): (1,10),(1,10),(2,14) → recta y = 4x + 6
    // P2 (unidad 2) = 14, C3/P3 (unidad 3) = 18
    // acumulado = 10*.15 + 10*.15 + 14*.15 + 14*.15 + 18*.20 + 18*.20 = 14.4
    expect(sandbox.projection(c)).toBeCloseTo(14.4, 6);
  });

  it('una evaluación con 0% de peso no influye en la proyección', () => {
    // Caso real: continuas 30/30/40 y parciales 0%. C1=17 y P1=0 → el P1 (0%) se ignora
    // y lo que falta se proyecta en 17, no en el promedio 8,5 (que daba 11,05).
    const c = makeCourse({ weights: [0.3, 0, 0.3, 0, 0.4, 0], grades: [17, 0, null, null, null, null] });
    expect(sandbox.projection(c)).toBeCloseTo(17, 6);
    expect(sandbox.trendSlope(c)).toBe(0);
  });

  it('dentro de una misma unidad, la nota con más porcentaje pesa más en la proyección', () => {
    // C1=10 (10%) y P1=16 (40%): promedio ponderado de la unidad = (1 + 6.4) / 0.5 = 14.8
    // Lo que falta (50%) se proyecta en 14.8 → total = 7.4 + 7.4 = 14.8
    const c = makeCourse({ weights: [0.1, 0.4, 0.2, 0.1, 0.1, 0.1], grades: [10, 16, null, null, null, null] });
    expect(sandbox.projection(c)).toBeCloseTo(14.8, 6);
  });

  it('si todas las notas ingresadas pesan 0%, usa peso igual como respaldo (no devuelve null)', () => {
    const c = makeCourse({ weights: [0.5, 0, 0.5, 0, 0, 0], grades: [null, 15, null, null, null, null] });
    expect(sandbox.projection(c)).not.toBeNull();
  });

  it('C2 y P2 faltantes reciben el mismo valor proyectado (misma unidad)', () => {
    const c = makeCourse({ grades: [19, 17, null, null, null, null] });
    const s = sandbox.courseSeries(c);
    expect(s.proj[2]).toBeCloseTo(18, 6);
    expect(s.proj[3]).toBeCloseTo(18, 6);
  });
});

describe('trendSlope / trendArrow', () => {
  it('devuelve pendiente 0 (flecha plana) con menos de 2 notas', () => {
    const c = makeCourse({ grades: [15, null, null, null, null, null] });
    expect(sandbox.trendSlope(c)).toBe(0);
    expect(sandbox.trendArrow(c).cls).toBe('trend-flat');
  });

  it('dos notas de la misma unidad no generan tendencia (flecha plana)', () => {
    const c = makeCourse({ grades: [19, 17, null, null, null, null] });
    expect(sandbox.trendSlope(c)).toBe(0);
    expect(sandbox.trendArrow(c).cls).toBe('trend-flat');
  });

  it('detecta tendencia al alza entre unidades', () => {
    const c = makeCourse({ grades: [10, 10, 14, 14, null, null] });
    expect(sandbox.trendSlope(c)).toBeCloseTo(4, 6);
    expect(sandbox.trendArrow(c)).toEqual({ icon: '↑', cls: 'trend-up' });
  });

  it('detecta tendencia a la baja entre unidades', () => {
    const c = makeCourse({ grades: [16, 16, 12, 12, null, null] });
    expect(sandbox.trendArrow(c)).toEqual({ icon: '↓', cls: 'trend-down' });
  });
});

describe('roundGrade', () => {
  it('redondea al entero más cercano', () => {
    expect(sandbox.roundGrade(12.2)).toBe(12);
    expect(sandbox.roundGrade(12.5)).toBe(13);
    expect(sandbox.roundGrade(12.8)).toBe(13);
  });

  it('corrige imprecisiones de punto flotante antes de redondear', () => {
    // 12.49999999999996 "debería" ser 12.5 conceptualmente; sin el toFixed(4)
    // previo, Math.round lo bajaría a 12 en vez de subirlo a 13.
    expect(sandbox.roundGrade(12.49999999999996)).toBe(13);
  });

  it('devuelve 0 para null/NaN', () => {
    expect(sandbox.roundGrade(null)).toBe(0);
    expect(sandbox.roundGrade(NaN)).toBe(0);
  });
});

describe('courseFinalValue', () => {
  it('usa el acumulado cuando ya no queda peso pendiente', () => {
    const c = makeCourse({ grades: [12, 12, 12, 12, 12, 12] });
    expect(sandbox.courseFinalValue(c)).toBeCloseTo(12, 6);
  });

  it('usa la proyección cuando falta peso por ingresar', () => {
    const c = makeCourse({ grades: [16, null, null, null, null, null] });
    expect(sandbox.courseFinalValue(c)).toBeCloseTo(sandbox.projection(c), 6);
  });
});

describe('passStatus', () => {
  it('Aprobado cuando el curso está completo y el acumulado alcanza 10.5', () => {
    const c = makeCourse({ grades: [12, 12, 12, 12, 12, 12] });
    const st = sandbox.passStatus(c);
    expect(st.level).toBe('ok');
    expect(st.label).toBe('Aprobado');
  });

  it('Desaprobado cuando el curso está completo y no alcanza 10.5', () => {
    const c = makeCourse({ grades: [9, 9, 9, 9, 9, 9] });
    const st = sandbox.passStatus(c);
    expect(st.level).toBe('danger');
    expect(st.label).toBe('Desaprobado');
  });

  it('"No podrás aprobar" si ni sacando 20 en lo restante se llega a 10.5', () => {
    const c = makeCourse({ grades: [0, 0, 0, 0, null, null] });
    const st = sandbox.passStatus(c);
    expect(st.level).toBe('danger');
    expect(st.label).toBe('No podrás aprobar');
  });

  it('"Aprobación asegurada" cuando ya se acumuló el mínimo aunque falte peso', () => {
    const c = makeCourse({ grades: [20, 20, 20, 20, null, null] });
    const st = sandbox.passStatus(c);
    expect(st.level).toBe('ok');
    expect(st.label).toBe('Aprobación asegurada');
  });

  it('"En curso" indica cuánto se necesita en cada evaluación pendiente', () => {
    const c = makeCourse({ grades: [10, 10, null, null, null, null] });
    const st = sandbox.passStatus(c);
    expect(st.level).toBe('neutral');
    expect(st.label).toBe('En curso');
    expect(st.detail).toContain('11'); // ceil((10.5-3)/0.7) = 11
  });
});

describe('metaStatus', () => {
  it('Meta alcanzada limpiamente cuando el curso está completo', () => {
    const c = makeCourse({ grades: [12, 12, 12, 12, 12, 12], goal: 11 });
    const st = sandbox.metaStatus(c);
    expect(st.level).toBe('ok');
    expect(st.label).toBe('Meta alcanzada');
    expect(st.clean).toBe(true);
  });

  it('Meta alcanzada solo por redondeo (advertencia)', () => {
    // acumulado 10.6 con curso completo: no llega a 11 "limpio", pero redondea a 11.
    const c = makeCourse({
      grades: [11, 11, 11, 11, 10, 10], // pesos .15*4 + .2*2 → acc = 6.6+4 = 10.6
      goal: 11,
    });
    const st = sandbox.metaStatus(c);
    expect(sandbox.acumulado(c)).toBeCloseTo(10.6, 6);
    expect(st.level).toBe('warn');
    expect(st.label).toBe('Meta alcanzada (por redondeo)');
  });

  it('Meta no alcanzada cuando ni redondeando se llega', () => {
    const c = makeCourse({ grades: [8, 8, 8, 8, 8, 8], goal: 11 });
    const st = sandbox.metaStatus(c);
    expect(st.level).toBe('danger');
    expect(st.label).toBe('Meta no alcanzada');
  });

  it('"sin datos aún" cuando no hay ninguna nota y falta peso', () => {
    const c = makeCourse({ goal: 11 });
    const st = sandbox.metaStatus(c);
    expect(st.level).toBe('neutral');
    expect(st.label).toBe('Meta: sin datos aún');
  });

  it('"Vas bien para tu meta" cuando la proyección ya alcanza la meta', () => {
    const c = makeCourse({ grades: [18, null, null, null, null, null], goal: 11 });
    const st = sandbox.metaStatus(c);
    expect(st.level).toBe('ok');
    expect(st.label).toBe('Vas bien para tu meta');
  });

  it('"En riesgo" cuando la proyección no alcanza ni redondeando', () => {
    const c = makeCourse({ grades: [5, null, null, null, null, null], goal: 11 });
    const st = sandbox.metaStatus(c);
    expect(st.level).toBe('danger');
    expect(st.label).toBe('En riesgo de no llegar a tu meta');
  });
});

describe('applySustitutorio', () => {
  it('falla si no hay Parcial 1 ni Parcial 2 ingresados', () => {
    const c = makeCourse();
    const res = sandbox.applySustitutorio(c, 18);
    expect(res.ok).toBe(false);
    expect(c.sustitutorio).toBeNull();
  });

  it('reemplaza el parcial más bajo cuando ambos están ingresados', () => {
    const c = makeCourse({ grades: [null, 10, null, 15, null, null] });
    const res = sandbox.applySustitutorio(c, 18);
    expect(res.ok).toBe(true);
    expect(c.sustitutorio).toEqual({ value: 18, targetIndex: 1, originalValue: 10 });
  });

  it('no aplica si el sustitutorio no supera la nota más baja', () => {
    const c = makeCourse({ grades: [null, 10, null, 15, null, null] });
    const res = sandbox.applySustitutorio(c, 9);
    expect(res.ok).toBe(false);
    expect(c.sustitutorio).toBeNull();
  });

  it('apunta a Parcial 2 (índice 3) cuando falta Parcial 1', () => {
    const c = makeCourse({ grades: [null, null, null, 12, null, null] });
    const res = sandbox.applySustitutorio(c, 15);
    expect(res.ok).toBe(true);
    expect(c.sustitutorio.targetIndex).toBe(3);
  });

  it('apunta a Parcial 1 (índice 1) cuando falta Parcial 2', () => {
    const c = makeCourse({ grades: [null, 12, null, null, null, null] });
    const res = sandbox.applySustitutorio(c, 15);
    expect(res.ok).toBe(true);
    expect(c.sustitutorio.targetIndex).toBe(1);
  });
});

describe('creditWeightedAvg / aggregateSeries', () => {
  it('ignora cursos sin créditos y pondera por crédito', () => {
    const a = makeCourse({ grades: [12, 12, 12, 12, 12, 12], creditos: 3 });
    const b = makeCourse({ grades: [16, 16, 16, 16, 16, 16], creditos: 1 });
    const zeroCredit = makeCourse({ grades: [0, 0, 0, 0, 0, 0], creditos: 0 });
    const avg = sandbox.creditWeightedAvg([a, b, zeroCredit], sandbox.acumulado);
    // (12*3 + 16*1) / 4 = 13
    expect(avg).toBeCloseTo(13, 6);
  });

  it('devuelve null si ningún curso tiene créditos', () => {
    const a = makeCourse({ grades: [12, 12, 12, 12, 12, 12], creditos: 0 });
    expect(sandbox.creditWeightedAvg([a], sandbox.acumulado)).toBeNull();
  });
});
