import 'package:flutter/material.dart';

class BottomBar extends StatelessWidget {
  final IconData? icon1;
  final void Function()? onTapIcon1;
  final void Function()? onTapIcon2;
  final void Function()? onTapIcon3;
  final void Function()? onTapIcon4;

  const BottomBar({
    Key? key,
    this.icon1,
    this.onTapIcon1,
    this.onTapIcon2,
    this.onTapIcon3,
    this.onTapIcon4,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.only(top: 10, bottom: 5),
      decoration: const BoxDecoration(color: Colors.blue),
      child: Wrap(
        alignment: WrapAlignment.spaceAround,
        children: [
          GestureDetector(
            child: Column(
              children: [
                Icon(
                  icon1 ?? Icons.volume_up,
                  size: 36,
                  semanticLabel: 'Botão Ler',
                  color: Colors.white,
                ),
                const SizedBox(height: 5),
                const Text(
                  'Ler',
                  semanticsLabel: '',
                  style: TextStyle(color: Colors.white),
                ),
              ],
            ),
            onTap: onTapIcon1,
          ),
          GestureDetector(
            child: Column(
              children: const [
                Icon(
                  Icons.dialpad,
                  size: 36,
                  semanticLabel: 'Botão Braille Tinta',
                  color: Colors.white,
                ),
                SizedBox(height: 5),
                Text(
                  'Braille/Tinta',
                  semanticsLabel: '',
                  style: TextStyle(color: Colors.white),
                ),
              ],
            ),
            onTap: onTapIcon2,
          ),
          GestureDetector(
            child: Column(
              children: const [
                Icon(
                  Icons.file_download,
                  size: 36,
                  semanticLabel: 'Botão Importar',
                  color: Colors.white,
                ),
                SizedBox(height: 5),
                Text(
                  'Importar',
                  semanticsLabel: '',
                  style: TextStyle(color: Colors.white),
                ),
              ],
            ),
            onTap: onTapIcon3,
          ),
          GestureDetector(
            child: Column(
              children: const [
                Icon(
                  Icons.file_upload,
                  size: 36,
                  semanticLabel: 'Botão Exportar',
                  color: Colors.white,
                ),
                SizedBox(height: 5),
                Text(
                  'Exportar',
                  semanticsLabel: '',
                  style: TextStyle(color: Colors.white),
                ),
              ],
            ),
            onTap: onTapIcon4,
          ),
        ],
      ),
    );
  }
}
