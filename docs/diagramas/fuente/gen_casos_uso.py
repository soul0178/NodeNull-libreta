"""Genera docs/img/01-casos-de-uso.{svg,png,jpg}: diagrama UML de casos de uso de Libreta."""
import sys
from playwright.sync_api import sync_playwright

# (id, texto) en el orden vertical; CU-08 queda junto a CU-11 por la relación <<include>>
CASOS = [
    ("CU-01", "Gestionar cursos"), ("CU-02", "Registrar notas"), ("CU-03", "Ver proyección y estado"),
    ("CU-04", "Gestionar meta"), ("CU-05", "Simular nota necesaria"), ("CU-06", "Aplicar sustitutorio"),
    ("CU-07", "Gestionar carreras"), ("CU-08", "Cerrar o reabrir semestre"), ("CU-11", "Ver logros"),
    ("CU-09", "Registrar semestre pasado"), ("CU-10", "Ver promedios y gráficos"),
    ("CU-12", "Exportar e importar respaldo"), ("CU-13", "Exportar reporte PDF"), ("CU-14", "Sincronizar con la nube"),
]
INCLUDES = [("CU-02", "CU-03"), ("CU-08", "CU-11")]
W, H0, STEP = 760, 110, 64
CX, OW, OH = 400, 280, 44
ACTOR = (90, 0)
H = H0 + STEP * len(CASOS) + 40
pos = {cid: (CX, H0 + i * STEP) for i, (cid, _) in enumerate(CASOS)}
ay = H // 2
gy = pos["CU-14"][1]

def actor(x, y, label):
    return (f'<g stroke="#222" stroke-width="2" fill="none"><circle cx="{x}" cy="{y-34}" r="12"/>'
            f'<line x1="{x}" y1="{y-22}" x2="{x}" y2="{y+14}"/><line x1="{x-22}" y1="{y-10}" x2="{x+22}" y2="{y-10}"/>'
            f'<line x1="{x}" y1="{y+14}" x2="{x-18}" y2="{y+42}"/><line x1="{x}" y1="{y+14}" x2="{x+18}" y2="{y+42}"/></g>'
            f'<text x="{x}" y="{y+64}" text-anchor="middle" font-size="15" font-weight="600">{label}</text>')

s = [f'<svg xmlns="http://www.w3.org/2000/svg" width="{W}" height="{H}" viewBox="0 0 {W} {H}" '
     f'font-family="Helvetica, Arial, sans-serif" font-size="14">',
     f'<rect width="{W}" height="{H}" fill="white"/>',
     f'<rect x="230" y="30" width="340" height="{H-60}" fill="#fafaff" stroke="#555" stroke-width="2"/>',
     '<text x="400" y="62" text-anchor="middle" font-size="17" font-weight="700">Sistema Libreta</text>',
     '<defs><marker id="a" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="8" markerHeight="8" orient="auto">'
     '<path d="M0,0 L10,5 L0,10" fill="none" stroke="#444" stroke-width="1.5"/></marker></defs>']
for cid, _ in CASOS:                       # asociaciones actor -> caso de uso
    x, y = pos[cid]
    s.append(f'<line x1="{ACTOR[0]+22}" y1="{ay-10}" x2="{x-OW//2}" y2="{y}" stroke="#444" stroke-width="1.2"/>')
x, y = pos["CU-14"]                        # asociación con actor secundario
s.append(f'<line x1="{x+OW//2}" y1="{y}" x2="{W-90}" y2="{gy-10}" stroke="#444" stroke-width="1.2"/>')
for a, b in INCLUDES:                      # <<include>>
    (xa, ya), (xb, yb) = pos[a], pos[b]
    s.append(f'<line x1="{xa+60}" y1="{ya+OH//2}" x2="{xb+60}" y2="{yb-OH//2-1}" stroke="#444" stroke-dasharray="6 4" marker-end="url(#a)"/>')
    s.append(f'<text x="{xa+66}" y="{(ya+yb)//2+4}" font-size="12" font-style="italic">&lt;&lt;include&gt;&gt;</text>')
for cid, text in CASOS:                    # óvalos
    x, y = pos[cid]
    s.append(f'<ellipse cx="{x}" cy="{y}" rx="{OW//2}" ry="{OH//2}" fill="#e9ecff" stroke="#5b5fc7" stroke-width="1.6"/>')
    s.append(f'<text x="{x}" y="{y+5}" text-anchor="middle">{cid} {text}</text>')
s.append(actor(ACTOR[0], ay, "Estudiante"))
s.append(actor(W - 90, gy, "Google / Firebase"))
s.append('</svg>')
svg = "\n".join(s)
open("img/01-casos-de-uso.svg", "w", encoding="utf-8").write(svg)
with sync_playwright() as p:
    b = p.chromium.launch(); pg = b.new_page(viewport={"width": W, "height": H}, device_scale_factor=2)
    pg.set_content(f'<body style="margin:0">{svg}</body>'); pg.screenshot(path="img/01-casos-de-uso.png"); b.close()
from PIL import Image
Image.open("img/01-casos-de-uso.png").convert("RGB").save("img/01-casos-de-uso.jpg", quality=92)
print("ok")
