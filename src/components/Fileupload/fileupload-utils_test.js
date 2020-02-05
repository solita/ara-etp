import { expect } from 'chai';

import * as Either from '../../utils/either-utils';
import * as FileUploadUtils from './fileupload-utils';

describe('FileUploadUtils:', () => {
  describe('isValidAmountFiles', () => {
    it('should return true with multiple false and only one value', () => {
      const multiple = false;
      const files = ['file1'];

      const expected = true;
      expect(FileUploadUtils.isValidAmountFiles(multiple, files)).to.eql(
        expected
      );
    });

    it('should return false with multiple false and multiple values', () => {
      const multiple = false;
      const files = ['file1', 'file2'];

      const expected = false;
      expect(FileUploadUtils.isValidAmountFiles(multiple, files)).to.eql(
        expected
      );
    });

    it('should return true with multiple true and one value', () => {
      const multiple = true;
      const files = ['file1'];

      const expected = true;
      expect(FileUploadUtils.isValidAmountFiles(multiple, files)).to.eql(
        expected
      );
    });

    it('should return true with multiple true and multiple values', () => {
      const multiple = true;
      const files = ['file1', 'file2'];

      const expected = true;
      expect(FileUploadUtils.isValidAmountFiles(multiple, files)).to.eql(
        expected
      );
    });
  });

  describe('multipleFileError', () => {
    it('should return right when no error is found', () => {
      const files = ['file1'];
      const predicate = () => true;
      const errorState = { error: 'no error should be found' };

      const expected = Either.Right(files);
      expect(FileUploadUtils.filesError(predicate, errorState, files)).to.eql(
        expected
      );
    });

    it('should return left when error is detected', () => {
      const files = ['file1'];
      const predicate = () => false;
      const errorState = { error: 'error should be found' };

      const expected = Either.Left(errorState);
      expect(FileUploadUtils.filesError(predicate, errorState, files)).to.eql(
        expected
      );
    });
  });

  describe('validFilesInput', () => {
    it('should return right when no transition causes errors', () => {
      const stateTransitions = [
        FileUploadUtils.filesError(() => true, {
          error: 'no error should be found'
        }),
        FileUploadUtils.filesError(FileUploadUtils.isValidAmountFiles(false), {
          error: 'no error should be found'
        })
      ];

      const files = ['file1'];
      const expected = Either.Right(files);
      expect(FileUploadUtils.validFilesInput(stateTransitions, files)).to.eql(
        expected
      );
    });

    it('should return left when transition causes errors', () => {
      const stateTransitions = [
        FileUploadUtils.filesError(() => true, {
          error: 'no error should be found'
        }),
        FileUploadUtils.filesError(FileUploadUtils.isValidAmountFiles(false), {
          error: 'amount not valid'
        })
      ];

      const files = ['file1', 'file2'];
      const expected = Either.Left({
        error: 'amount not valid'
      });
      expect(FileUploadUtils.validFilesInput(stateTransitions, files)).to.eql(
        expected
      );
    });

    it('should return left of first error encountered', () => {
      const stateTransitions = [
        FileUploadUtils.filesError(FileUploadUtils.isValidAmountFiles(false), {
          error: 'amount not valid'
        }),
        FileUploadUtils.filesError(() => false, {
          error: 'this error should not return'
        })
      ];

      const files = ['file1', 'file2'];
      const expected = Either.Left({
        error: 'amount not valid'
      });
      expect(FileUploadUtils.validFilesInput(stateTransitions, files)).to.eql(
        expected
      );
    });
  });
});
