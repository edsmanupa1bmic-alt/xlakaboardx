/**
 * InlineCalculator
 * Ported from org.fossify.keyboard.helpers.InlineCalculator
 * Automatically detects math expressions ending with "=" (e.g. "250*4=", "120+450=")
 * and returns calculated result to be shown in the suggestion Smartbar.
 */

export class InlineCalculator {
  static calculate(expression: string): string | null {
    if (!expression || !expression.endsWith('=')) return null;

    const cleanExpr = expression.slice(0, -1).trim();
    if (!cleanExpr) return null;

    // Support two-operand expressions like 250*4, 150.5+20, 100/4, 50-20
    const binaryRegex = /^(-?\d+(?:\.\d+)?)\s*([+\-*/^%])\s*(-?\d+(?:\.\d+)?)$/;
    const match = cleanExpr.match(binaryRegex);

    if (match) {
      const num1 = parseFloat(match[1]);
      const op = match[2];
      const num2 = parseFloat(match[3]);

      if (isNaN(num1) || isNaN(num2)) return null;

      let result: number;
      switch (op) {
        case '+':
          result = num1 + num2;
          break;
        case '-':
          result = num1 - num2;
          break;
        case '*':
          result = num1 * num2;
          break;
        case '/':
          if (num2 === 0) return 'Error (div by 0)';
          result = num1 / num2;
          break;
        case '^':
          result = Math.pow(num1, num2);
          break;
        case '%':
          result = num1 % num2;
          break;
        default:
          return null;
      }

      // Round cleanly to avoid floating point imprecision like 0.30000000000000004
      const rounded = Number(Math.round(Number(result + 'e8')) + 'e-8');
      return String(rounded);
    }

    // Also support safe multi-term math parsing if valid arithmetic characters only
    if (/^[0-9+\-*/().\s%]+$/.test(cleanExpr)) {
      try {
        // Safe evaluation without eval
        const sanitized = cleanExpr.replace(/[^0-9+\-*/().%]/g, '');
        // eslint-disable-next-line no-new-func
        const fn = new Function(`return (${sanitized})`);
        const res = fn();
        if (typeof res === 'number' && !isNaN(res) && isFinite(res)) {
          const rounded = Number(Math.round(Number(res + 'e8')) + 'e-8');
          return String(rounded);
        }
      } catch {
        return null;
      }
    }

    return null;
  }
}
