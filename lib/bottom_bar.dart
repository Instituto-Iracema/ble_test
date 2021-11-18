import 'package:flutter/material.dart';

class BottomBar extends StatelessWidget {
  final void Function()? onTapIcon1;
  final void Function()? onTapIcon2;
  final void Function()? onTapIcon3;
  final void Function()? onTapIcon4;

  const BottomBar({
    Key? key,
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
              children: const [
                Icon(
                  Icons.radio_button_checked,
                  size: 36,
                  semanticLabel: 'Botão 1',
                  color: Colors.white,
                ),
                SizedBox(height: 5),
                Text(
                  'Botão 1',
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
                  Icons.radio_button_off,
                  size: 36,
                  semanticLabel: 'Botão 2',
                  color: Colors.white,
                ),
                SizedBox(height: 5),
                Text(
                  'Botão 2',
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
                  Icons.radio_button_on,
                  size: 36,
                  semanticLabel: 'Botão 3',
                  color: Colors.white,
                ),
                SizedBox(height: 5),
                Text(
                  'Botão 3',
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
                  Icons.radio_button_unchecked,
                  size: 36,
                  semanticLabel: 'Botão 4',
                  color: Colors.white,
                ),
                SizedBox(height: 5),
                Text(
                  'Botão 4',
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
