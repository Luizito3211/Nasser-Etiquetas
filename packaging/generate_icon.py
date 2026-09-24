"""
Script para geração de ícone .ico multi-resolução para o empacotamento Windows (jpackage).
Gera packaging/icone.ico com os tamanhos: 16, 32, 48, 64, 128, 256 px a partir da imagem original.
"""
from pathlib import Path
from PIL import Image

def generate_ico():
    base_dir = Path(__file__).resolve().parent.parent
    source_img = base_dir / "src" / "main" / "resources" / "img" / "LogoStage.Jpeg"
    output_ico = base_dir / "packaging" / "icone.ico"

    if not source_img.exists():
        raise FileNotFoundError(f"Imagem de origem não encontrada: {source_img}")

    img = Image.open(source_img)
    # Converter para RGBA se necessário para preservar qualidade
    if img.mode != "RGBA":
        img = img.convert("RGBA")

    sizes = [(16, 16), (32, 32), (48, 48), (64, 64), (128, 128), (256, 256)]
    img.save(output_ico, format="ICO", sizes=sizes)
    print(f"Ícone gerado com sucesso em: {output_ico} com resoluções {sizes}")

if __name__ == "__main__":
    generate_ico()
