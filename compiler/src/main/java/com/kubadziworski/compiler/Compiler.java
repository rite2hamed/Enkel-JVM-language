package com.kubadziworski.compiler;

import com.kubadziworski.domain.global.CompilationUnit;
import com.kubadziworski.bytecodegenerator.BytecodeGenerator;
import com.kubadziworski.exception.CompilationException;
import com.kubadziworski.parsing.Parser;
import com.kubadziworski.validation.ARGUMENT_ERRORS;
import org.apache.commons.io.IOUtils;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Paths;

/**
 * Created by kuba on 15.03.16.
 */
public class Compiler implements Opcodes {

    private static final Logger LOGGER = LoggerFactory.getLogger(Compiler.class);

    public static void main(String[] args) throws Exception {
        try {
            new Compiler().compile(args);
        } catch (Exception exception) {
            LOGGER.error("ERROR: " + exception.getMessage());
        }
    }

    public void compile(String[] args) throws Exception {
        ARGUMENT_ERRORS argumentsErrors = getArgumentValidationErrors(args);
        if (argumentsErrors != ARGUMENT_ERRORS.NONE) {
            String errorMessage = argumentsErrors.getMessage();
            LOGGER.error(errorMessage);
            return;
        }
        File enkelFile = new File(args[0]);
        String fileAbsolutePath = enkelFile.getAbsolutePath();
        LOGGER.info("Trying to parse '{}'.", enkelFile.getAbsolutePath());
        CompilationUnit compilationUnit = new Parser().getCompilationUnit(fileAbsolutePath);
        LOGGER.info("Finished Parsing. Started compiling to bytecode.");
        saveBytecodeToClassFile(compilationUnit);
    }

    private ARGUMENT_ERRORS getArgumentValidationErrors(String[] args) {
        if (args.length != 1) {
            return ARGUMENT_ERRORS.NO_FILE;
        }
        String filePath = args[0];
        if (!filePath.endsWith(".enk")) {
            return ARGUMENT_ERRORS.BAD_FILE_EXTENSION;
        }
        return ARGUMENT_ERRORS.NONE;
    }

    private void saveBytecodeToClassFile(CompilationUnit compilationUnit) throws IOException {
        BytecodeGenerator bytecodeGenerator = new BytecodeGenerator();
        byte[] bytecode = bytecodeGenerator.generate(compilationUnit);
        String className = compilationUnit.getClassName();
        String fileName = className + ".class";
        LOGGER.info("Finished Compiling. Saving bytecode to '{}'.", Paths.get(fileName).toAbsolutePath());
        OutputStream os = new FileOutputStream(fileName);
        IOUtils.write(bytecode, os);
        LOGGER.info("Done. To run compiled file execute: 'java {}' in current dir",className);
    }
}