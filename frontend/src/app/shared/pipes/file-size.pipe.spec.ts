import { FileSizePipe } from './file-size.pipe';

describe('FileSizePipe', () => {
  let pipe: FileSizePipe;

  beforeEach(() => {
    pipe = new FileSizePipe();
  });

  it('transforms 0 to "0 B"', () => {
    expect(pipe.transform(0)).toBe('0 B');
  });

  it('transforms null and undefined to "0 B"', () => {
    expect(pipe.transform(null)).toBe('0 B');
    expect(pipe.transform(undefined)).toBe('0 B');
  });

  it('transforms bytes to KB', () => {
    expect(pipe.transform(1024)).toBe('1 KB');
    expect(pipe.transform(2048)).toBe('2 KB');
  });

  it('transforms bytes to MB', () => {
    expect(pipe.transform(1048576)).toBe('1 MB');
    expect(pipe.transform(5242880)).toBe('5 MB');
  });

  it('transforms bytes to GB', () => {
    expect(pipe.transform(1073741824)).toBe('1 GB');
  });
});
